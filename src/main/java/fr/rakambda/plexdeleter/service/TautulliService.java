package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import fr.rakambda.plexdeleter.web.webhook.tautulli.data.TautulliWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class TautulliService{
	private final WatchService watchService;
	private final MediaService mediaService;
	private final UserPersonRepository userPersonRepository;
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final TautulliApiService tautulliApiService;
	private final NotificationService notificationService;
	private final MediaRequirementService mediaRequirementService;
	
	public TautulliService(WatchService watchService, MediaService mediaService, UserPersonRepository userPersonRepository, MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository, TautulliApiService tautulliApiService, NotificationService notificationService, MediaRequirementService mediaRequirementService){
		this.watchService = watchService;
		this.mediaService = mediaService;
		this.userPersonRepository = userPersonRepository;
		this.mediaRepository = mediaRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.tautulliApiService = tautulliApiService;
		this.notificationService = notificationService;
		this.mediaRequirementService = mediaRequirementService;
	}
	
	public void updateRequirement(@NonNull TautulliWebhook data) throws RequestFailedException, IOException{
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
	
	public void updateMedia(@NonNull TautulliWebhook data) throws RequestFailedException, UpdateException, NotifyException{
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
	
	public void notifyMedia(@NonNull TautulliWebhook data) throws RequestFailedException, NotifyException{
		var ratingKey = data.getRatingKey();
		if(Objects.isNull(ratingKey)){
			log.warn("Not notifying any media, could not determine rating key from {}", data);
			return;
		}
		
		var metadata = tautulliApiService.getMetadata(ratingKey).getResponse().getData();
		if(Objects.isNull(metadata)){
			log.warn("Not notifying any media, could not get metadata from {}", data);
			return;
		}
		notificationService.notifyMediaAdded(metadata);
	}
	
	public void addNewMediaIfPreviousExist(@NonNull TautulliWebhook data) throws RequestFailedException, NotifyException, UpdateException, ThymeleafMessageException{
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
		
		var metadata = tautulliApiService.getMetadata(ratingKey).getResponse().getData();
		if(Objects.isNull(metadata)){
			log.warn("Not adding any media, could not get metadata from {}", data);
			return;
		}
		
		var rootGuid = Optional.ofNullable(metadata.getGrandparentGuid())
				.or(() -> Optional.ofNullable(metadata.getParentGuid()))
				.orElseGet(metadata::getGuid);
		var mediaIndex = switch(Objects.requireNonNull(metadata.getMediaType())){
			case MOVIE, SHOW, ARTIST -> 1;
			case SEASON -> metadata.getMediaIndex();
			case EPISODE, TRACK, PHOTO -> metadata.getParentMediaIndex();
		};
		
		var previous = Optional
				.ofNullable(rootGuid).flatMap(id -> mediaRepository.findByPlexGuidAndIndex(id, mediaIndex - 1))
				.or(() -> Optional.ofNullable(data.getTvdbId()).flatMap(id -> mediaRepository.findByTmdbIdAndIndex(id, mediaIndex - 1)))
				.or(() -> Optional.ofNullable(data.getTmdbId()).flatMap(id -> mediaRepository.findByTvdbIdAndIndex(id, mediaIndex - 1)));
		if(previous.isEmpty()){
			return;
		}
		
		var addedDate = Optional.ofNullable(data.getUtcTime()).map(t -> t.minusSeconds(10)).orElseGet(Instant::now);
		var media = mediaService.addMediaFromPrevious(previous.get(), mediaIndex, addedDate);
		media.setPlexId(ratingKey);
		media = mediaRepository.save(media);
		
		mediaRequirementService.addRequirementForNewMedia(media, null);
	}
}
