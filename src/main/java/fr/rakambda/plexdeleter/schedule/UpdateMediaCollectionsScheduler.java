package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.rest.PmsApiService;
import fr.rakambda.plexdeleter.api.plex.rest.data.Metadata;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpdateMediaCollectionsScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final PmsApiService pmsApiService;
	
	@Autowired
	public UpdateMediaCollectionsScheduler(MediaRepository mediaRepository, PmsApiService pmsApiService){
		this.mediaRepository = mediaRepository;
		this.pmsApiService = pmsApiService;
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "media-collections-update";
	}
	
	@Override
	@Scheduled(cron = "0 45 0,8,15 * * *")
	@Transactional
	public void run(){
		log.info("Updating media collections");
		var medias = mediaRepository.findAllByStatusIn(MediaStatus.allOnDisk()).stream()
				.filter(m -> Objects.nonNull(m.getPlexId()))
				.toList();
		
		for(var media : medias){
			try{
				updateCollections(media);
			}
			catch(RequestFailedException e){
				log.error("Failed to update media collections {}", media, e);
			}
		}
		
		log.info("Done updating {} media collections", medias.size());
	}
	
	private void updateCollections(@NonNull MediaEntity media) throws RequestFailedException{
		log.info("Updating media collections for {}", media);
		int ratingKey = Objects.requireNonNull(media.getPlexId());
		
		var collections = media.getRequirements().stream()
				.filter(mr -> mr.getStatus().isWantToWatchMore())
				.map(MediaRequirementEntity::getGroup)
				.filter(UserGroupEntity::getAppearInCollections)
				.map(UserGroupEntity::getName)
				.collect(Collectors.toSet());
		
		var currentCollections = pmsApiService.getElementMetadata(ratingKey).getMediaContainer().getMetadata().stream()
				.map(Metadata::getCollection)
				.flatMap(Collection::stream)
				.map(fr.rakambda.plexdeleter.api.plex.rest.data.Collection::getTag)
				.collect(Collectors.toSet());
		
		if(currentCollections.equals(collections)){
			log.info("Collections are already correct");
			return;
		}
		
		pmsApiService.setElementCollections(ratingKey, collections);
	}
}
