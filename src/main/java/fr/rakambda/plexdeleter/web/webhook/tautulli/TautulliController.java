package fr.rakambda.plexdeleter.web.webhook.tautulli;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import fr.rakambda.plexdeleter.web.webhook.tautulli.data.TautulliWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/webhook/tautulli")
public class TautulliController{
	private final WatchService watchService;
	private final MediaService mediaService;
	private final UserPersonRepository userPersonRepository;
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	public TautulliController(WatchService watchService, MediaService mediaService, UserPersonRepository userPersonRepository, MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository){
		this.watchService = watchService;
		this.mediaService = mediaService;
		this.userPersonRepository = userPersonRepository;
		this.mediaRepository = mediaRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody TautulliWebhook data) throws RequestFailedException, IOException, UpdateException, NotifyException{
		switch(data.getType()){
			case "watched" -> updateRequirement(data);
			case "added" -> updateMedia(data);
		}
	}
	
	private void updateRequirement(@NotNull TautulliWebhook data) throws RequestFailedException, IOException{
		if(Objects.isNull(data.getUserId())){
			log.warn("Not updating any requirement, could not determine user id from {}", data);
			return;
		}
		
		var ratingKey = switch(data.getType()){
			case "movie" -> data.getRatingKey();
			case "episode" -> data.getParentRatingKey();
			default -> null;
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
	
	private void updateMedia(TautulliWebhook data) throws RequestFailedException, UpdateException, NotifyException{
		var ratingKey = switch(data.getType()){
			case "movie" -> data.getRatingKey();
			case "episode" -> data.getParentRatingKey();
			default -> null;
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not updating any media, could not determine rating key from {}", data);
			return;
		}
		
		var mediaEntity = mediaRepository.findByPlexId(ratingKey);
		if(mediaEntity.isEmpty()){
			log.warn("Not updating any media, could not find media from {}", data);
			return;
		}
		
		mediaService.update(mediaEntity.get());
	}
}
