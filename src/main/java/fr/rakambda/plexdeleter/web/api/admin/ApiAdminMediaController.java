package fr.rakambda.plexdeleter.web.api.admin;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/api/admin/media")
public class ApiAdminMediaController{
	private final MediaService mediaService;
	
	@Autowired
	public ApiAdminMediaController(MediaService mediaService){
		this.mediaService = mediaService;
	}
	
	@PostMapping("/delete")
	public ModelAndView delete(@NotNull @RequestParam("media") int mediaId) throws NotifyException, RequestFailedException{
		mediaService.deleteMedia(mediaId, true);
		return new ModelAndView("api/success");
	}
}
