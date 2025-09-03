package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetHistoryResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.HistoryRecord;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WatchService{
	private final TautulliApiService tautulliApiService;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public WatchService(TautulliApiService tautulliApiService, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository){
		this.tautulliApiService = tautulliApiService;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@NonNull
	public Map<Integer, List<HistoryRecord>> getGroupWatchHistory(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity mediaEntity, @Nullable Instant historySince) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			throw new RequestFailedException("Could not get info for media with null PlexId");
		}
		var history = new LinkedList<GetHistoryResponse>();
		for(var person : userGroupEntity.getPersons()){
			var data = tautulliApiService.getHistory(mediaEntity.getPlexId(), mediaEntity.getType(), person.getPlexId(), historySince).getResponse().getData();
			if(Objects.nonNull(data)){
				history.add(data);
			}
		}
		
		return history.stream()
				.map(GetHistoryResponse::getData)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(HistoryRecord::getMediaIndex));
	}
	
	public void update(@NonNull MediaRequirementEntity mediaRequirementEntity) throws RequestFailedException, IOException{
		log.info("Updating media requirement {}", mediaRequirementEntity);
		
		var media = mediaRequirementEntity.getMedia();
		var group = mediaRequirementEntity.getGroup();
		if(Objects.isNull(media.getPlexId())){
			log.warn("Cannot update media requirement {} as media does not seem to be in Plex/Tautulli", mediaRequirementEntity);
			return;
		}
		
		var historyPerPart = getGroupWatchHistory(group, media, mediaRequirementEntity.getLastCompletedTime());
		var watchedFullyCount = historyPerPart.values().stream()
				.filter(watches -> watches.stream().anyMatch(watch -> Objects.equals(watch.getWatchedStatus(), 1)))
				.count();
		
		mediaRequirementEntity.setWatchedCount(watchedFullyCount);
		if(media.getStatus().isFullyDownloaded() && watchedFullyCount >= media.getAvailablePartsCount()){
			log.info("Setting {} as watched", mediaRequirementEntity);
			mediaRequirementEntity.setStatus(MediaRequirementStatus.WATCHED);
			mediaRequirementEntity.setLastCompletedTime(Instant.now());
			supervisionService.send("\uD83D\uDC41\uFE0F %s watched %s", group.getName(), media);
		}
		
		mediaRequirementRepository.save(mediaRequirementEntity);
	}
}
