package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import fr.rakambda.plexdeleter.service.ThymeleafService;
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
	private final MediaRepository mediaRepository;
	private final ThymeleafService thymeleafService;
	
	@Autowired
	public AdminMediaRequirementController(AdminService adminService, UserGroupRepository userGroupRepository, MediaRepository mediaRepository, ThymeleafService thymeleafService){
		this.adminService = adminService;
		this.userGroupRepository = userGroupRepository;
		this.mediaRepository = mediaRepository;
		this.thymeleafService = thymeleafService;
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
		mav.addObject("thymeleafService", thymeleafService);
		mav.addObject("displayUsers", true);
		return mav;
	}
}
