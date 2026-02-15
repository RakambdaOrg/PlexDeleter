package fr.rakambda.plexdeleter.web.webhook.seerr;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.seerr.SeerrApiService;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrApiService;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.ServiceException;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import fr.rakambda.plexdeleter.web.webhook.seerr.data.Extra;
import fr.rakambda.plexdeleter.web.webhook.seerr.data.Media;
import fr.rakambda.plexdeleter.web.webhook.seerr.data.Request;
import fr.rakambda.plexdeleter.web.webhook.seerr.data.SeerrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/webhook/seerr")
public class SeerrController{
	private final MediaRepository mediaRepository;
	private final MediaRequirementService mediaRequirementService;
	private final MediaService mediaService;
	private final SeerrApiService seerrApiService;
	private final SonarrApiService sonarrApiService;
	private final RadarrApiService radarrApiService;
	private final UserGroupRepository userGroupRepository;
	private final String excludeTag;
	
	public SeerrController(MediaRepository mediaRepository, MediaRequirementService mediaRequirementService, MediaService mediaService, SeerrApiService seerrApiService, SonarrApiService sonarrApiService, RadarrApiService radarrApiService, UserGroupRepository userGroupRepository, ApplicationConfiguration applicationConfiguration){
		this.mediaRepository = mediaRepository;
		this.mediaRequirementService = mediaRequirementService;
		this.mediaService = mediaService;
		this.seerrApiService = seerrApiService;
		this.sonarrApiService = sonarrApiService;
		this.radarrApiService = radarrApiService;
		this.userGroupRepository = userGroupRepository;
		this.excludeTag = applicationConfiguration.getExcludeTag();
	}
	
	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody SeerrWebhook data) throws RequestFailedException, UpdateException, NotifyException, ServiceException, ThymeleafMessageException{
		log.info("Received new Seerr webhook {}", data);
		switch(data.getNotificationType()){
			case "MEDIA_AUTO_APPROVED", "MEDIA_APPROVED" -> onMediaApproved(data);
			case "MEDIA_AVAILABLE" -> onMediaAdded(data);
		}
	}
	
	private void onMediaAdded(@NonNull SeerrWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var seerrId = Optional.ofNullable(data.getMedia()).map(Media::getTmdbId);
		
		var medias = seerrId
				.map(mediaRepository::findAllBySeerrId)
				.orElseGet(() -> mediaRepository.findAllByStatusIn(MediaStatus.allNeedRefresh()));
		
		for(var media : medias){
			mediaService.update(media);
		}
	}
	
	private void onMediaApproved(@NonNull SeerrWebhook data) throws RequestFailedException, UpdateException, NotifyException, ThymeleafMessageException{
		var requestId = Optional.ofNullable(data.getRequest()).map(Request::getRequestId);
		if(requestId.isEmpty()){
			log.warn("Not adding any media, could not determine request id from {}", data);
			return;
		}
		
		var requestDetails = seerrApiService.getRequestDetails(requestId.get());
		var requestDetailsMedia = Objects.requireNonNull(requestDetails.getMedia());
		var seerrId = requestDetailsMedia.getTmdbId();
		var plexUserId = requestDetails.getRequestedBy().getPlexId();
		
		var tags = switch(requestDetailsMedia.getMediaType()){
			case MOVIE -> radarrApiService.getTags();
			case TV -> sonarrApiService.getTags();
		};
		var mappedTags = tags.stream().collect(Collectors.toMap(Tag::getId, Tag::getLabel));
		
		if(Optional.ofNullable(requestDetails.getTags()).orElse(Set.of()).stream()
				.map(mappedTags::get)
				.anyMatch(name -> Objects.equals(name, excludeTag))){
			log.warn("Not adding any media, it is excluded by tag {}", data);
			return;
		}
		
		var seasons = switch(requestDetailsMedia.getMediaType()){
			case MOVIE -> List.of(1);
			case TV -> data.getExtra().stream()
					.filter(extra -> Objects.equals(extra.getName(), "Requested Seasons"))
					.map(Extra::getValue)
					.map(value -> value.split(","))
					.flatMap(Arrays::stream)
					.map(String::trim)
					.map(Integer::parseInt)
					.sorted()
					.toList();
		};
		
		var userGroupEntity = userGroupRepository.findByContainingPlexUserId(plexUserId);
		if(userGroupEntity.isEmpty()){
			log.warn("Not adding any media, could not determine user group from {}", data);
			return;
		}
		
		var mediaType = switch(requestDetailsMedia.getMediaType()){
			case MOVIE -> MediaType.MOVIE;
			case TV -> MediaType.SEASON;
		};
		
		for(var season : seasons){
			var mediaId = mediaService.addMedia(seerrId, mediaType, season, null);
			mediaRequirementService.addRequirementForNewMedia(mediaId, userGroupEntity.get());
		}
	}
}
