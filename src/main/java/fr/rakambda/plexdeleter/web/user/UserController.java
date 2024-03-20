package fr.rakambda.plexdeleter.web.user;

import fr.rakambda.plexdeleter.security.PlexUser;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController{
	private final UserPersonRepository userPersonRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final ThymeleafService thymeleafService;
	
	@Autowired
	public UserController(UserPersonRepository userPersonRepository, MediaRequirementRepository mediaRequirementRepository, ThymeleafService thymeleafService){
		this.userPersonRepository = userPersonRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.thymeleafService = thymeleafService;
	}
	
	@GetMapping(value = "/home")
	public ModelAndView index(@NotNull Authentication authentication){
		var plexId = Optional.ofNullable(authentication.getPrincipal())
				.filter(PlexUser.class::isInstance)
				.map(PlexUser.class::cast)
				.map(PlexUser::getPlexId)
				.orElseThrow(() -> new IllegalStateException("Could not get Plex id from authorization"));
		
		var userPerson = userPersonRepository.findByPlexId(plexId)
				.orElseThrow(() -> new IllegalStateException("Could not find user with Plex id %d".formatted(plexId)));
		
		var requirements = mediaRequirementRepository.findAllByIdGroupIdAndStatusIs(userPerson.getGroupId(), MediaRequirementStatus.WAITING).stream()
				.sorted(MediaRequirementEntity.COMPARATOR_BY_MEDIA)
				.toList();
		
		var mav = new ModelAndView("user/home");
		mav.addObject("requirements", requirements);
		mav.addObject("thymeleafService", thymeleafService);
		return mav;
	}
}
