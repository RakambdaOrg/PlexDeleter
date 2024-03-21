package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.AdminService;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin/media")
public class AdminMediaController{
	private final AdminService adminService;
	private final ThymeleafService thymeleafService;
	
	@Autowired
	public AdminMediaController(AdminService adminService, ThymeleafService thymeleafService){
		this.adminService = adminService;
		this.thymeleafService = thymeleafService;
	}
	
	@GetMapping("/soon-deleted")
	public ModelAndView listSoonDeleted(){
		var mav = new ModelAndView("admin/media/soon-deleted");
		mav.addObject("medias", adminService.getSoonDeletedMedias());
		mav.addObject("thymeleafService", thymeleafService);
		return mav;
	}
}
