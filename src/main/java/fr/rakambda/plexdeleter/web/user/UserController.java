package fr.rakambda.plexdeleter.web.user;

import fr.rakambda.plexdeleter.security.PlexUser;
import fr.rakambda.plexdeleter.service.UserService;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController{
	private final UserService userService;
	private final UserPersonRepository userPersonRepository;
	
	@Autowired
	public UserController(UserService userService, UserPersonRepository userPersonRepository){
		this.userService = userService;
		this.userPersonRepository = userPersonRepository;
	}
	
	@RequestMapping(value = "/home")
	public ModelAndView index(@NotNull Authentication authentication){
		var plexId = Optional.ofNullable(authentication.getPrincipal())
				.filter(PlexUser.class::isInstance)
				.map(PlexUser.class::cast)
				.map(PlexUser::getPlexId)
				.orElseThrow(() -> new IllegalStateException("Could not get Plex id from authorization"));
		
		var userPerson = userPersonRepository.findByPlexId(plexId)
				.orElseThrow(() -> new IllegalStateException("Could not find user with Plex id %d".formatted(plexId)));
		
		var mav = new ModelAndView("user/index");
		mav.addObject("userService", userService);
		mav.addObject("person", userPerson);
		return mav;
	}
}
