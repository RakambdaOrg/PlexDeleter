package fr.rakambda.plexdeleter.web.api.user;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.security.PlexUser;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.ServiceException;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user/media-requirement")
public class ApiUserMediaRequirementController{
	private final MediaRequirementService mediaRequirementService;
	private final UserPersonRepository userPersonRepository;
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public ApiUserMediaRequirementController(MediaRequirementService mediaRequirementService, UserPersonRepository userPersonRepository, MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository){
		this.mediaRequirementService = mediaRequirementService;
		this.userPersonRepository = userPersonRepository;
		this.mediaRepository = mediaRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@Transactional
	@RequestMapping(value = "/add", method = {
			RequestMethod.GET,
			RequestMethod.POST
	})
	public ModelAndView add(@NonNull Authentication authentication, @NotNull @RequestParam("media") int mediaId) throws NotifyException, RequestFailedException, UpdateException, ThymeleafMessageException{
		var userPerson = getUserPersonEntityFromAuth(authentication);
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaRequirementService.addRequirementForNewMedia(media, userPerson.getGroup());
		return new ModelAndView("/api/success");
	}
	
	@Transactional
	@PostMapping("/abandon")
	public ModelAndView abandon(@NonNull Authentication authentication, @NotNull @RequestParam("media") int mediaId) throws NotifyException, RequestFailedException{
		var userPerson = getUserPersonEntityFromAuth(authentication);
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, userPerson.getGroupId()))
				.orElseThrow(() -> new RuntimeException("Requirement not found"));
		mediaRequirementService.abandon(requirement);
		return new ModelAndView("/api/success");
	}
	
	@Transactional
	@PostMapping("/complete")
	public ModelAndView complete(@NonNull Authentication authentication, @NotNull @RequestParam("media") int mediaId) throws NotifyException, ServiceException{
		var userPerson = getUserPersonEntityFromAuth(authentication);
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, userPerson.getGroupId()))
				.orElseThrow(() -> new RuntimeException("Requirement not found"));
		mediaRequirementService.complete(requirement);
		return new ModelAndView("/api/success");
	}
	
	private UserPersonEntity getUserPersonEntityFromAuth(@NonNull Authentication authentication){
		var plexId = Optional.ofNullable(authentication.getPrincipal())
				.filter(PlexUser.class::isInstance)
				.map(PlexUser.class::cast)
				.map(PlexUser::getPlexId)
				.orElseThrow(() -> new IllegalStateException("Could not get Plex id from authorization"));
		
		return userPersonRepository.findByPlexId(plexId)
				.orElseThrow(() -> new IllegalStateException("Could not find user with Plex id %d".formatted(plexId)));
	}
}
