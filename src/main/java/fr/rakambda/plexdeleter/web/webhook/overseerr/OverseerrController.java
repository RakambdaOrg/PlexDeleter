package fr.rakambda.plexdeleter.web.webhook.overseerr;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrApiService;
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
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Extra;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Media;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.OverseerrWebhook;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
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
@RequestMapping("/webhook/overseerr")
public class OverseerrController{
	private final MediaRepository mediaRepository;
	private final MediaRequirementService mediaRequirementService;
	private final MediaService mediaService;
	private final OverseerrApiService overseerrApiService;
	private final SonarrApiService sonarrApiService;
	private final RadarrApiService radarrApiService;
	private final UserGroupRepository userGroupRepository;
	private final String excludeTag;

	public OverseerrController(MediaRepository mediaRepository, MediaRequirementService mediaRequirementService, MediaService mediaService, OverseerrApiService overseerrApiService, SonarrApiService sonarrApiService, RadarrApiService radarrApiService, UserGroupRepository userGroupRepository, ApplicationConfiguration applicationConfiguration){
		this.mediaRepository = mediaRepository;
		this.mediaRequirementService = mediaRequirementService;
		this.mediaService = mediaService;
		this.overseerrApiService = overseerrApiService;
		this.sonarrApiService = sonarrApiService;
		this.radarrApiService = radarrApiService;
		this.userGroupRepository = userGroupRepository;
		this.excludeTag = applicationConfiguration.getExcludeTag();
	}
	
	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException, ServiceException, ThymeleafMessageException{
		log.info("Received new Overseerr webhook {}", data);
		switch(data.getNotificationType()){
			case "MEDIA_AUTO_APPROVED", "MEDIA_APPROVED" -> onMediaApproved(data);
			case "MEDIA_AVAILABLE" -> onMediaAdded(data);
		}
	}
	
	private void onMediaAdded(@org.jspecify.annotations.NonNull OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var overseerrId = Optional.ofNullable(data.getMedia()).map(Media::getTmdbId);
		
		var medias = overseerrId
				.map(mediaRepository::findAllByOverseerrId)
				.orElseGet(() -> mediaRepository.findAllByStatusIn(MediaStatus.allNeedRefresh()));
		
		for(var media : medias){
			mediaService.update(media);
		}
	}
	
	private void onMediaApproved(@org.jspecify.annotations.NonNull OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException, ThymeleafMessageException{
		var requestId = Optional.ofNullable(data.getRequest()).map(Request::getRequestId);
		if(requestId.isEmpty()){
			log.warn("Not adding any media, could not determine request id from {}", data);
			return;
		}

		var requestDetails = overseerrApiService.getRequestDetails(requestId.get());
		var requestDetailsMedia = Objects.requireNonNull(requestDetails.getMedia());
		var overseerrId = requestDetailsMedia.getTmdbId();
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
			var mediaId = mediaService.addMedia(overseerrId, mediaType, season, null);
			mediaRequirementService.addRequirementForNewMedia(mediaId, userGroupEntity.get());
		}
	}
}
