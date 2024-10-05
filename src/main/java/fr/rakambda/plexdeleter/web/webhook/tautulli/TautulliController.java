package fr.rakambda.plexdeleter.web.webhook.tautulli;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import fr.rakambda.plexdeleter.web.webhook.tautulli.data.TautulliWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/webhook/tautulli")
public class TautulliController{
	private final WatchService watchService;
	private final MediaService mediaService;
	private final UserPersonRepository userPersonRepository;
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final TautulliService tautulliService;
	private final NotificationService notificationService;
	private final MediaRequirementService mediaRequirementService;
	
	public TautulliController(WatchService watchService, MediaService mediaService, UserPersonRepository userPersonRepository, MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository, TautulliService tautulliService, NotificationService notificationService, MediaRequirementService mediaRequirementService){
		this.watchService = watchService;
		this.mediaService = mediaService;
		this.userPersonRepository = userPersonRepository;
		this.mediaRepository = mediaRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.tautulliService = tautulliService;
		this.notificationService = notificationService;
		this.mediaRequirementService = mediaRequirementService;
	}
	
	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody TautulliWebhook data) throws RequestFailedException, IOException, UpdateException, NotifyException, ThymeleafMessageException{
		log.info("Received new Tautulli webhook {}", data);
		
		if(!data.getMediaType().isNotifyAdded()){
			return;
		}
		
		switch(data.getType()){
			case "watched" -> {
				updateRequirement(data);
				addNewMediaIfPreviousExist(data);
			}
			case "added" -> {
				updateMedia(data);
				notifyMedia(data);
				addNewMediaIfPreviousExist(data);
			}
		}
	}
	
	private void updateRequirement(@NotNull TautulliWebhook data) throws RequestFailedException, IOException{
		if(Objects.isNull(data.getUserId())){
			log.warn("Not updating any requirement, could not determine user id from {}", data);
			return;
		}
		var ratingKey = switch(data.getMediaType()){
			case MOVIE, SHOW, SEASON, ARTIST -> data.getRatingKey();
			case EPISODE, TRACK, PHOTO -> data.getParentRatingKey();
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not updating any requirement, could not determine rating key from {}", data);
			return;
		}
		
		var userPersonEntity = userPersonRepository.findByPlexId(data.getUserId());
		if(userPersonEntity.isEmpty()){
			log.warn("Not updating any requirement, could not find user person from {}", data);
			return;
		}
		
		var mediaEntity = mediaRepository.findByPlexId(ratingKey);
		if(mediaEntity.isEmpty()){
			log.warn("Not updating any requirement, could not find media from {}", data);
			return;
		}
		
		var requirementId = new MediaRequirementEntity.TableId(mediaEntity.get().getId(), userPersonEntity.get().getGroupId());
		var mediaRequirementEntity = mediaRequirementRepository.findById(requirementId);
		if(mediaRequirementEntity.isEmpty()){
			log.warn("Not updating any requirement, could not find requirement with id {} from {}", requirementId, data);
			return;
		}
		
		watchService.update(mediaRequirementEntity.get());
	}
	
	private void updateMedia(@NotNull TautulliWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var ratingKey = switch(Objects.requireNonNull(data.getMediaType())){
			case MOVIE, SEASON, SHOW, ARTIST -> data.getRatingKey();
			case EPISODE, TRACK, PHOTO -> data.getParentRatingKey();
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not updating any media, could not determine rating key from {}", data);
			return;
		}
		
		var mediaEntity = mediaRepository.findByPlexId(ratingKey);
		if(mediaEntity.isEmpty()){
			mediaService.updateAll();
			return;
		}
		
		mediaService.update(mediaEntity.get());
	}
	
	private void notifyMedia(@NotNull TautulliWebhook data) throws RequestFailedException, NotifyException{
		var ratingKey = data.getRatingKey();
		if(Objects.isNull(ratingKey)){
			log.warn("Not notifying any media, could not determine rating key from {}", data);
			return;
		}
		
		var metadata = tautulliService.getMetadata(ratingKey).getResponse().getData();
		if(Objects.isNull(metadata)){
			log.warn("Not notifying any media, could not get metadata from {}", data);
			return;
		}
		notificationService.notifyMediaAdded(metadata);
	}
	
	private void addNewMediaIfPreviousExist(@NotNull TautulliWebhook data) throws RequestFailedException, NotifyException, UpdateException, ThymeleafMessageException{
		var ratingKey = switch(Objects.requireNonNull(data.getMediaType())){
			case MOVIE, SEASON, SHOW, ARTIST -> data.getRatingKey();
			case EPISODE, TRACK, PHOTO -> data.getParentRatingKey();
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not adding any media, could not determine rating key from {}", data);
			return;
		}
		
		var mediaEntity = mediaRepository.findByPlexId(ratingKey);
		if(mediaEntity.isPresent()){
			return;
		}
		
		var metadata = tautulliService.getMetadata(ratingKey).getResponse().getData();
		if(Objects.isNull(metadata)){
			log.warn("Not adding any media, could not get metadata from {}", data);
			return;
		}
		
		var rootRatingKey = Optional.ofNullable(metadata.getGrandparentRatingKey())
				.or(() -> Optional.ofNullable(metadata.getParentRatingKey()))
				.orElseGet(metadata::getRatingKey);
		var mediaIndex = switch(Objects.requireNonNull(data.getMediaType())){
			case MOVIE, SHOW, ARTIST -> 1;
			case SEASON -> metadata.getMediaIndex();
			case EPISODE, TRACK, PHOTO -> metadata.getParentMediaIndex();
		};
		
		var previous = Optional.of(rootRatingKey).flatMap(id -> mediaRepository.findByRootPlexIdAndIndex(rootRatingKey, mediaIndex - 1))
				.or(() -> Optional.ofNullable(data.getTvdbId()).flatMap(id -> mediaRepository.findByTmdbIdAndIndex(id, mediaIndex - 1)))
				.or(() -> Optional.ofNullable(data.getTmdbId()).flatMap(id -> mediaRepository.findByTvdbIdAndIndex(id, mediaIndex - 1)));
		if(previous.isEmpty()){
			return;
		}
		
		var media = mediaService.addMediaFromPrevious(previous.get(), mediaIndex);
		media.setPlexId(ratingKey);
		media = mediaRepository.save(media);
		
		mediaRequirementService.addRequirementForNewMedia(media, null);
	}
}
