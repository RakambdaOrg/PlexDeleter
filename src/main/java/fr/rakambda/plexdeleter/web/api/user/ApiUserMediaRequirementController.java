package fr.rakambda.plexdeleter.web.api.user;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.security.PlexUser;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.ServiceException;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	@Autowired
	public ApiUserMediaRequirementController(MediaRequirementService mediaRequirementService, UserPersonRepository userPersonRepository){
		this.mediaRequirementService = mediaRequirementService;
		this.userPersonRepository = userPersonRepository;
	}
	
	@PostMapping("/abandon")
	public ModelAndView abandon(@org.jetbrains.annotations.NotNull Authentication authentication, @NotNull @RequestParam("media") int mediaId) throws NotifyException, RequestFailedException{
		var userPerson = getUserPersonEntityFromAuth(authentication);
		mediaRequirementService.abandon(mediaId, userPerson.getGroupId());
		return new ModelAndView("/api/success");
	}
	
	@PostMapping("/complete")
	public ModelAndView complete(@org.jetbrains.annotations.NotNull Authentication authentication, @NotNull @RequestParam("media") int mediaId) throws NotifyException, ServiceException{
		var userPerson = getUserPersonEntityFromAuth(authentication);
		mediaRequirementService.complete(mediaId, userPerson.getGroupId());
		return new ModelAndView("/api/success");
	}
	
	private UserPersonEntity getUserPersonEntityFromAuth(@org.jetbrains.annotations.NotNull Authentication authentication){
		var plexId = Optional.ofNullable(authentication.getPrincipal())
				.filter(PlexUser.class::isInstance)
				.map(PlexUser.class::cast)
				.map(PlexUser::getPlexId)
				.orElseThrow(() -> new IllegalStateException("Could not get Plex id from authorization"));
		
		return userPersonRepository.findByPlexId(plexId)
				.orElseThrow(() -> new IllegalStateException("Could not find user with Plex id %d".formatted(plexId)));
	}
}
