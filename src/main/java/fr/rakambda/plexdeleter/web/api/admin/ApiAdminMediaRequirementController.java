package fr.rakambda.plexdeleter.web.api.admin;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaRequirementService;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.ServiceException;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/api/admin/media-requirement")
public class ApiAdminMediaRequirementController{
	private final MediaService mediaService;
	private final MediaRequirementService mediaRequirementService;
	private final UserGroupRepository userGroupRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public ApiAdminMediaRequirementController(MediaService mediaService, MediaRequirementService mediaRequirementService, UserGroupRepository userGroupRepository, MediaRequirementRepository mediaRequirementRepository){
		this.mediaService = mediaService;
		this.mediaRequirementService = mediaRequirementService;
		this.userGroupRepository = userGroupRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@Transactional
	@PostMapping("/add")
	public ModelAndView add(
			@NotNull @RequestParam("group") int groupId,
			@NotNull @RequestParam("overseerr") int overseerrId,
			@NotNull @RequestParam("season") int season,
			@Nullable @RequestParam("episode") Integer episode,
			@NotNull @RequestParam("type") MediaType type
	) throws NotifyException, RequestFailedException, UpdateException, ServiceException, ThymeleafMessageException{
		var userGroupEntity = userGroupRepository.findById(groupId)
				.orElseThrow(() -> new IllegalArgumentException("Could not find user group with id %d".formatted(groupId)));
		
		var media = mediaService.addMedia(overseerrId, type, season, episode);
		mediaRequirementService.addRequirementForNewMedia(media, userGroupEntity);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/complete")
	public ModelAndView complete(@NotNull @RequestParam("mediaId") int mediaId, @NotNull @RequestParam("groupId") int groupId) throws NotifyException, ServiceException{
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, groupId))
				.orElseThrow(() -> new RuntimeException("Requirement not found"));
		mediaRequirementService.complete(requirement);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/abandon")
	public ModelAndView abandon(@NotNull @RequestParam("mediaId") int mediaId, @NotNull @RequestParam("groupId") int groupId) throws NotifyException, RequestFailedException{
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, groupId))
				.orElseThrow(() -> new RuntimeException("Requirement not found"));
		mediaRequirementService.abandon(requirement);
		return new ModelAndView("api/success");
	}
}
