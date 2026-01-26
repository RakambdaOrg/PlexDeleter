package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.gql.PlexCommunityApiService;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityWatchHistory;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.service.data.WatchState;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WatchService{
	private final TautulliApiService tautulliApiService;
	private final PlexCommunityApiService plexCommunityApiService;
	
	@Autowired
	public WatchService(TautulliApiService tautulliApiService, PlexCommunityApiService plexCommunityApiService){
		this.tautulliApiService = tautulliApiService;
		this.plexCommunityApiService = plexCommunityApiService;
	}
	
	@NonNull
	public Map<Integer, Boolean> getGroupWatchHistory(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity mediaEntity, @Nullable Instant historySince) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			throw new RequestFailedException("Could not get info for media with null PlexId");
		}
		var history = new LinkedList<WatchState>();
		var mediaPlexId = mediaEntity.getPlexId();
		
		history.addAll(getWatchStateFromTautulli(userGroupEntity, mediaEntity, mediaPlexId, historySince));
		history.addAll(getWatchStateFromPlex(userGroupEntity, mediaPlexId, historySince));
		
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
							.map(h -> new WatchState(Optional.ofNullable(h.getMediaIndex()).orElse(1), h.getWatchedStatus() == 1))
							.toList());
				}
			}
		}
		catch(Exception e){
			log.error("Failed to get watch history from Tautulli", e);
		}
		return history;
	}
	
	@NonNull
	private List<WatchState> getWatchStateFromPlex(@NonNull UserGroupEntity userGroupEntity, int mediaPlexId, @Nullable Instant historySince){
		var history = new LinkedList<WatchState>();
		try{
			var metadataId = Optional.ofNullable(tautulliApiService.getMetadata(mediaPlexId).getResponse().getData()).map(GetMetadataResponse::getGuidId).orElse(null);
			var userIds = userGroupEntity.getPersons().stream()
					.map(UserPersonEntity::getCommunityId)
					.filter(Objects::nonNull)
					.toList();
			if(Objects.nonNull(metadataId) && !userIds.isEmpty()){
				history.addAll(plexCommunityApiService.listActivityForItem(metadataId, historySince).stream()
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
}
