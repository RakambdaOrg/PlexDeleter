package fr.rakambda.plexdeleter.web.webhook.overseerr;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaType;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrService;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaActionStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Extra;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Media;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.OverseerrWebhook;
import fr.rakambda.plexdeleter.web.webhook.overseerr.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/webhook/overseerr")
public class OverseerrController{
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	private final OverseerrService overseerrService;
	private final SonarrService sonarrService;
	private final RadarrService radarrService;
	private final UserGroupRepository userGroupRepository;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final NotificationService notificationService;
	private final String excludeTag;
	
	public OverseerrController(MediaRepository mediaRepository, MediaService mediaService, OverseerrService overseerrService, SonarrService sonarrService, RadarrService radarrService, UserGroupRepository userGroupRepository, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository, NotificationService notificationService, ApplicationConfiguration applicationConfiguration){
		this.mediaRepository = mediaRepository;
		this.mediaService = mediaService;
		this.overseerrService = overseerrService;
		this.sonarrService = sonarrService;
		this.radarrService = radarrService;
		this.userGroupRepository = userGroupRepository;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.notificationService = notificationService;
		this.excludeTag = applicationConfiguration.getExcludeTag();
	}
	
	@PostMapping("/")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		switch(data.getNotificationType()){
			case "MEDIA_AUTO_APPROVED", "MEDIA_APPROVED" -> onMediaApproved(data);
			case "MEDIA_AVAILABLE" -> onMediaAdded(data);
		}
	}
	
	private void onMediaAdded(@NotNull OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var tvdbId = Optional.ofNullable(data.getMedia()).map(Media::getTvdbId);
		
		var medias = tvdbId
				.map(id -> mediaRepository.findAllByTvdbIdAndAvailability(id, MediaAvailability.DOWNLOADING))
				.orElseGet(() -> mediaRepository.findAllByAvailability(MediaAvailability.DOWNLOADING));
		
		for(var media : medias){
			mediaService.update(media);
		}
	}
	
	private void onMediaApproved(@NotNull OverseerrWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var requestId = Optional.ofNullable(data.getRequest()).map(Request::getRequestId);
		if(requestId.isEmpty()){
			log.warn("Not adding any media, could not determine request id from {}", data);
			return;
		}
		
		var requestDetails = overseerrService.getRequestDetails(requestId.get());
		var overseerrId = requestDetails.getMedia().getId();
		var plexUserId = requestDetails.getRequestedBy().getPlexId();
		
		var userGroupEntity = userGroupRepository.findByContainingPlexUserId(plexUserId);
		if(userGroupEntity.isEmpty()){
			log.warn("Not adding any media, could not determine user group from {}", data);
			return;
		}
		
		var tags = switch(requestDetails.getMedia().getMediaType()){
			case MOVIE -> radarrService.getTags();
			case TV -> sonarrService.getTags();
		};
		var mappedTags = tags.stream().collect(Collectors.toMap(Tag::getId, Tag::getLabel));
		
		if(requestDetails.getTags().stream()
				.map(mappedTags::get)
				.anyMatch(name -> Objects.equals(name, excludeTag))){
			log.warn("Not adding any media, it is excluded by tag {}", data);
			return;
		}
		
		var seasons = switch(requestDetails.getMedia().getMediaType()){
			case MOVIE -> List.of(1);
			case TV -> data.getExtra().stream()
					.filter(extra -> Objects.equals(extra.getName(), "Requested Seasons"))
					.map(Extra::getValue)
					.map(value -> value.split(","))
					.flatMap(Arrays::stream)
					.map(Integer::parseInt)
					.toList();
		};
		
		for(var season : seasons){
			var media = getOrCreateMedia(overseerrId, requestDetails.getMedia().getMediaType(), season);
			media.setAvailability(MediaAvailability.DOWNLOADING);
			media.setActionStatus(MediaActionStatus.TO_DELETE);
			mediaRepository.save(media);
			
			media = mediaService.update(media);
			
			addRequirement(media, userGroupEntity.get(), true);
			
			var otherGroups = userGroupRepository.findAllByHasRequirementOn(Objects.requireNonNull(media.getOverseerrId()), media.getIndex() - 1);
			for(var otherGroup : otherGroups){
				if(Objects.equals(otherGroup.getId(), userGroupEntity.get().getId())){
					continue;
				}
				addRequirement(media, otherGroup, false);
			}
		}
	}
	
	private void addRequirement(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity, boolean allowModify) throws NotifyException{
		var id = new MediaRequirementEntity.TableId(media.getId(), userGroupEntity.getId());
		if(!mediaRequirementRepository.existsById(id)){
			mediaRequirementRepository.save(MediaRequirementEntity.builder()
					.id(id)
					.status(MediaRequirementStatus.WAITING)
					.build());
			supervisionService.send("Added requirement %s to %s", media, userGroupEntity);
			notificationService.notifyRequirementAdded(userGroupEntity, media);
			return;
		}
		
		if(!allowModify){
			return;
		}
		
		mediaRequirementRepository.findById(id)
				.map(requirement -> {
					requirement.setStatus(MediaRequirementStatus.WAITING);
					return requirement;
				})
				.ifPresent(mediaRequirementRepository::save);
		supervisionService.send("Updated requirement %s to %s", media, userGroupEntity);
		notificationService.notifyRequirementAdded(userGroupEntity, media);
	}
	
	@NotNull
	private MediaEntity getOrCreateMedia(int overseerrId, @NotNull MediaType mediaType, int season) throws RequestFailedException{
		var mediaDetails = overseerrService.getMediaDetails(overseerrId, mediaType);
		return mediaRepository.findByOverseerrIdAndIndex(overseerrId, season)
				.orElseGet(() -> createMedia(overseerrId, mediaType, season, mediaDetails));
	}
	
	@NotNull
	private MediaEntity createMedia(int overseerrId, @NotNull MediaType mediaType, int season, @NotNull fr.rakambda.plexdeleter.api.overseerr.data.Media mediaDetails){
		supervisionService.send("Added media"); //TODO
		return MediaEntity.builder()
				.type(switch(mediaType){
					case MOVIE -> fr.rakambda.plexdeleter.storage.entity.MediaType.MOVIE;
					case TV -> fr.rakambda.plexdeleter.storage.entity.MediaType.SEASON;
				})
				.overseerrId(overseerrId)
				.name(switch(mediaDetails){
					case MovieMedia movieMedia -> movieMedia.getTitle();
					case SeriesMedia seriesMedia -> seriesMedia.getName();
					default -> throw new IllegalStateException("Unexpected value: " + mediaDetails);
				})
				.index(season)
				.availability(MediaAvailability.DOWNLOADING)
				.actionStatus(MediaActionStatus.TO_DELETE)
				.build();
	}
}
