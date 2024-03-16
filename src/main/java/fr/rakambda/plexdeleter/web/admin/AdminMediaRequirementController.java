package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import fr.rakambda.plexdeleter.service.UserService;
import fr.rakambda.plexdeleter.storage.entity.MediaActionStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.Set;

@RestController
@RequestMapping("/admin/media-requirement")
public class AdminMediaRequirementController{
	private final AdminService adminService;
	private final UserGroupRepository userGroupRepository;
	private final UserService userService;
	private final MediaRepository mediaRepository;
	
	@Autowired
	public AdminMediaRequirementController(AdminService adminService, UserGroupRepository userGroupRepository, UserService userService, MediaRepository mediaRepository){
		this.adminService = adminService;
		this.userGroupRepository = userGroupRepository;
		this.userService = userService;
		this.mediaRepository = mediaRepository;
	}
	
	@GetMapping("/add")
	public ModelAndView add(){
		var mav = new ModelAndView("admin/media-requirement/add");
		mav.addObject("groups", userGroupRepository.findAll());
		mav.addObject("medias", mediaRepository.findAllByActionStatusIn(Set.of(MediaActionStatus.TO_DELETE)));
		return mav;
	}
	
	@GetMapping("/list")
	public ModelAndView list(){
		var mav = new ModelAndView("admin/media-requirement/list");
		mav.addObject("requirements", adminService.getMediaRequirementsThatCanBeCompleted());
		mav.addObject("userService", userService);
		return mav;
	}
}
