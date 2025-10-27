package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.gql.PlexCommunityService;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityWatchHistory;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.service.data.WatchState;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WatchService{
	private final TautulliApiService tautulliApiService;
	private final PlexCommunityService plexCommunityService;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public WatchService(TautulliApiService tautulliApiService, PlexCommunityService plexCommunityService, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository){
		this.tautulliApiService = tautulliApiService;
		this.plexCommunityService = plexCommunityService;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@NonNull
	public Map<Integer, Boolean> getGroupWatchHistory(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity mediaEntity, @Nullable Instant historySince) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			throw new RequestFailedException("Could not get info for media with null PlexId");
		}
		var history = new LinkedList<WatchState>();
		var mediaPlexId = mediaEntity.getPlexId();
		
		history.addAll(getWatchStateFromTautulli(userGroupEntity, mediaEntity, mediaPlexId, historySince));
		history.addAll(getWatchStateFromPlex(userGroupEntity, mediaEntity, mediaPlexId, historySince));
		
		return history.stream().collect(Collectors.toMap(WatchState::index, WatchState::watched, Boolean::logicalOr));
	}
	
	@NonNull
	private List<WatchState> getWatchStateFromTautulli(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity mediaEntity, int mediaPlexId, @Nullable Instant historySince){
		var history = new LinkedList<WatchState>();
		try{
			for(var person : userGroupEntity.getPersons()){
				var data = tautulliApiService.getHistory(mediaPlexId, mediaEntity.getType(), person.getPlexId(), historySince).getResponse().getData();
				if(Objects.nonNull(data)){
					history.addAll(data.getData().stream()
							.map(h -> new WatchState(h.getMediaIndex(), h.getWatchedStatus() == 1))
							.toList());
				}
			}
		}
		catch(Exception e){
			log.error("Failed to get watch history from Tautulli");
		}
		return history;
	}
	
	@NonNull
	private List<WatchState> getWatchStateFromPlex(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity mediaEntity, int mediaPlexId, @Nullable Instant historySince){
		var history = new LinkedList<WatchState>();
		try{
			var metadataId = Optional.ofNullable(tautulliApiService.getMetadata(mediaPlexId).getResponse().getData()).map(GetMetadataResponse::getGuidId).orElse(null);
			var userIds = userGroupEntity.getPersons().stream()
					.map(UserPersonEntity::getCommunityId)
					.filter(Objects::nonNull)
					.toList();
			if(Objects.nonNull(metadataId) && !userIds.isEmpty()){
				history.addAll(plexCommunityService.listActivityForItem(metadataId, historySince).stream()
						.filter(a -> userIds.contains(a.getUserV2().getId()))
						.filter(ActivityWatchHistory.class::isInstance)
						.map(ActivityWatchHistory.class::cast)
						.filter(a -> Objects.isNull(historySince) || a.getDate().isAfter(historySince) || a.getDate().equals(historySince))
						.map(a -> new WatchState(a.getMetadataItem().getIndex(), true))
						.toList());
			}
		}
		catch(Exception e){
			log.error("Failed to get watch history from Plex Community", e);
		}
		return history;
	}
	
	public void update(@NonNull MediaRequirementEntity mediaRequirementEntity) throws RequestFailedException, IOException{
		log.info("Updating media requirement {}", mediaRequirementEntity);
		
		var media = mediaRequirementEntity.getMedia();
		var group = mediaRequirementEntity.getGroup();
		if(Objects.isNull(media.getPlexId())){
			log.warn("Cannot update media requirement {} as media does not seem to be in Plex/Tautulli", mediaRequirementEntity);
			return;
		}
		
		var historySince = Stream.of(mediaRequirementEntity.getLastCompletedTime(), media.getLastRequestedTime())
				.filter(Objects::nonNull)
				.min(Comparator.comparing(Function.identity()))
				.orElse(null);
		var historyPerPart = getGroupWatchHistory(group, media, historySince);
		var watchedFullyCount = historyPerPart.values().stream()
				.filter(watched -> watched)
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
