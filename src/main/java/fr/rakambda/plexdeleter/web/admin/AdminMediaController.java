package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin/media")
public class AdminMediaController{
	private final AdminService adminService;
	
	@Autowired
	public AdminMediaController(AdminService adminService){
		this.adminService = adminService;
	}
	
	@GetMapping("/delete")
	public ModelAndView delete(){
		var mav = new ModelAndView("admin/media/delete");
		mav.addObject("medias", adminService.getMediasThatCanBeDeleted());
		return mav;
	}
}
