package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin/media-requirement")
public class AdminMediaRequirementController{
	private final AdminService adminService;
	private final UserGroupRepository userGroupRepository;
	
	@Autowired
	public AdminMediaRequirementController(AdminService adminService, UserGroupRepository userGroupRepository){
		this.adminService = adminService;
		this.userGroupRepository = userGroupRepository;
	}
	
	@GetMapping("/add")
	public ModelAndView add(){
		var mav = new ModelAndView("admin/media-requirement/add");
		mav.addObject("groups", userGroupRepository.findAll());
		return mav;
	}
	
	@GetMapping("/complete")
	public ModelAndView complete(){
		var mav = new ModelAndView("admin/media-requirement/complete");
		mav.addObject("requirements", adminService.getMediaRequirementsThatCanBeCompleted());
		return mav;
	}
	
	@GetMapping("/abandon")
	public ModelAndView abandon(){
		var mav = new ModelAndView("admin/media-requirement/abandon");
		mav.addObject("requirements", adminService.getMediaRequirementsThatCanBeCompleted());
		return mav;
	}
}
