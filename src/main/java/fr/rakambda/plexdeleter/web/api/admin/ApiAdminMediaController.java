package fr.rakambda.plexdeleter.web.api.admin;

import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
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
@RequestMapping("/api/admin/media")
public class ApiAdminMediaController{
	private final MediaService mediaService;
	private final MediaRepository mediaRepository;
	
	@Autowired
	public ApiAdminMediaController(MediaService mediaService, MediaRepository mediaRepository){
		this.mediaService = mediaService;
		this.mediaRepository = mediaRepository;
	}
	
	@Transactional
	@PostMapping("/keep")
	public ModelAndView keep(@NotNull @RequestParam("mediaId") int mediaId){
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.keep(media);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/unkeep")
	public ModelAndView unkeep(@NotNull @RequestParam("mediaId") int mediaId){
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.unkeep(media);
		return new ModelAndView("api/success");
	}
}
