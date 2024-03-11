package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetHistoryResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.HistoryRecord;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WatchService{
	private final TautulliService tautulliService;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public WatchService(TautulliService tautulliService, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@NotNull
	public Map<Integer, List<HistoryRecord>> getGroupWatchHistory(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity mediaEntity) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			throw new RequestFailedException("Could not get info for media with null PlexId");
		}
		var history = new LinkedList<GetHistoryResponse>();
		for(var person : userGroupEntity.getPersons()){
			history.add(tautulliService.getHistory(mediaEntity.getPlexId(), mediaEntity.getType(), person.getPlexId()).getResponse().getData());
		}
		
		return history.stream()
				.map(GetHistoryResponse::getData)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(HistoryRecord::getMediaIndex));
	}
	
	public void update(@NotNull MediaRequirementEntity mediaRequirementEntity) throws RequestFailedException, IOException{
		log.info("Updating media requirement {}", mediaRequirementEntity);
		
		var media = mediaRequirementEntity.getMedia();
		var group = mediaRequirementEntity.getGroup();
		if(Objects.isNull(media.getPlexId())){
			log.warn("Cannot update media requirement {} as media does not seem to be in Plex/Tautulli", mediaRequirementEntity);
			return;
		}
		
		var historyPerPart = getGroupWatchHistory(group, media);
		var everythingWatchedFully = historyPerPart.values().stream()
				.allMatch(watches -> watches.stream()
						.anyMatch(watch -> Objects.equals(watch.getWatchedStatus(), 1)));
		
		if(everythingWatchedFully && historyPerPart.size() >= media.getPartsCount()){
			log.info("Setting {} as watched", mediaRequirementEntity);
			mediaRequirementEntity.setStatus(MediaRequirementStatus.WATCHED);
			supervisionService.send("\uD83D\uDC41 %s watched %s", group.getName(), media);
		}
		
		mediaRequirementRepository.save(mediaRequirementEntity);
	}
}
