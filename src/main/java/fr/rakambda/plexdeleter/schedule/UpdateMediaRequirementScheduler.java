package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.sonarr.SonarrService;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetHistoryResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.HistoryRecord;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpdateMediaRequirementScheduler implements IScheduler{
	private final TautulliService tautulliService;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public UpdateMediaRequirementScheduler(MediaRepository mediaRepository, OverseerrService overseerrService, TautulliService tautulliService, SonarrService sonarrService, RadarrService radarrService, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@Override
	@NotNull
	public String getTaskId(){
		return "media-requirement-update";
	}
	
	@Override
	@Scheduled(cron = "0 30 0,8,15 * * *")
	@Transactional
	public void run(){
		var requirements = mediaRequirementRepository.findAllByStatusIs(MediaRequirementStatus.WAITING);
		for(var requirement : requirements){
			try{
				update(requirement);
			}
			catch(RequestFailedException | IOException e){
				log.error("Failed to update media requirement {}", requirement, e);
			}
		}
		
		log.info("Done updating {} media requirements", requirements.size());
	}
	
	@VisibleForTesting
	void update(@NotNull MediaRequirementEntity mediaRequirementEntity) throws RequestFailedException, IOException{
		log.info("Updating media requirement {}", mediaRequirementEntity);
		
		var media = mediaRequirementEntity.getMedia();
		var group = mediaRequirementEntity.getGroup();
		if(Objects.isNull(media.getPlexId())){
			log.warn("Cannot update media requirement {} as media does not seem to be in Plex/Tautulli", mediaRequirementEntity);
			return;
		}
		
		var history = new LinkedList<GetHistoryResponse>();
		for(var person : group.getPersons()){
			history.add(tautulliService.getHistory(media.getPlexId(), media.getType(), person.getPlexId()).getResponse().getData());
		}
		
		var historyPerPart = history.stream()
				.map(GetHistoryResponse::getData)
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(HistoryRecord::getMediaIndex));
		
		var everythingWatchedFully = historyPerPart.values().stream()
				.allMatch(watches -> watches.stream()
						.anyMatch(watch -> Objects.equals(watch.getWatchedStatus(), 1)));
		if(everythingWatchedFully && historyPerPart.size() >= media.getPartsCount()){
			mediaRequirementEntity.setStatus(MediaRequirementStatus.WATCHED);
			supervisionService.send("\uD83D\uDC41 %s watched %s", group.getName(), media);
		}
		
		mediaRequirementRepository.save(mediaRequirementEntity);
	}
}
