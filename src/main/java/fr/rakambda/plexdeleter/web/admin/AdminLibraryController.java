package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.service.LibraryService;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/admin/library")
public class AdminLibraryController{
	private final ThymeleafService thymeleafService;
	private final LibraryService libraryService;
	
	@Autowired
	public AdminLibraryController(ThymeleafService thymeleafService, LibraryService libraryService){
		this.thymeleafService = thymeleafService;
		this.libraryService = libraryService;
	}
	
	@GetMapping("/unmapped")
	public ModelAndView listUnmapped(){
		var mav = new ModelAndView("admin/library/unmapped");
		mav.addObject("elements", libraryService.getAllLibraryContentWithoutMedia());
		mav.addObject("thymeleafService", thymeleafService);
		return mav;
	}
}
