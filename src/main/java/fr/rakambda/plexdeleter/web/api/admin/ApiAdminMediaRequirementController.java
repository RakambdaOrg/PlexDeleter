package fr.rakambda.plexdeleter.web.api.admin;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/media-requirement")
public class ApiAdminMediaRequirementController{
	private final MediaService mediaService;
	private final MediaRequirementService mediaRequirementService;
	private final UserGroupRepository userGroupRepository;
	
	@Autowired
	public ApiAdminMediaRequirementController(MediaService mediaService, MediaRequirementService mediaRequirementService, UserGroupRepository userGroupRepository){
		this.mediaService = mediaService;
		this.mediaRequirementService = mediaRequirementService;
		this.userGroupRepository = userGroupRepository;
	}
	
	@PostMapping("/add")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ModelAndView add(
			@NotNull @RequestParam("group") int groupId,
			@NotNull @RequestParam("overseerr") int overseerrId,
			@NotNull @RequestParam("season") int season,
			@NotNull @RequestParam("type") MediaType type
	) throws NotifyException, RequestFailedException, UpdateException{
		var userGroupEntity = userGroupRepository.findById(groupId)
				.orElseThrow(() -> new IllegalArgumentException("Could not find user group with id %d".formatted(groupId)));
		
		mediaService.addMedia(userGroupEntity, overseerrId, type, List.of(season));
		return new ModelAndView("redirect:/");
	}
	
	@PostMapping("/complete")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ModelAndView complete(@NotNull @NotBlank @Pattern(regexp = "\\d+\\|\\d+") @RequestParam("requirement") String requirement) throws NotifyException{
		var parts = requirement.split("\\|", 2);
		var mediaId = Integer.parseInt(parts[0]);
		var groupId = Integer.parseInt(parts[1]);
		mediaRequirementService.complete(mediaId, groupId);
		return new ModelAndView("redirect:/");
	}
	
	@PostMapping("/abandon")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ModelAndView abandon(@NotNull @NotBlank @Pattern(regexp = "\\d+\\|\\d+") @RequestParam("requirement") String requirement) throws NotifyException{
		var parts = requirement.split("\\|", 2);
		var mediaId = Integer.parseInt(parts[0]);
		var groupId = Integer.parseInt(parts[1]);
		mediaRequirementService.abandon(mediaId, groupId);
		return new ModelAndView("redirect:/");
	}
}
