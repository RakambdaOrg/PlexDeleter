package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import fr.rakambda.plexdeleter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin/media")
public class AdminMediaController{
	private final AdminService adminService;
	private final UserService userService;
	
	@Autowired
	public AdminMediaController(AdminService adminService, UserService userService){
		this.adminService = adminService;
		this.userService = userService;
	}
	
	@GetMapping("/soon-deleted")
	public ModelAndView listSoonDeleted(){
		var mav = new ModelAndView("admin/media/soon-deleted");
		mav.addObject("medias", adminService.getSoonDeletedMedias());
		mav.addObject("userService", userService);
		return mav;
	}
	
	@GetMapping("/delete")
	public ModelAndView delete(){
		var mav = new ModelAndView("admin/media/delete");
		mav.addObject("medias", adminService.getMediasThatCanBeDeleted());
		return mav;
	}
}
