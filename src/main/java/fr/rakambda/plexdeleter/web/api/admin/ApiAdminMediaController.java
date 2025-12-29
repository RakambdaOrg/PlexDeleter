package fr.rakambda.plexdeleter.web.api.admin;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
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
	
	@Transactional
	@PostMapping("/manual")
	public ModelAndView manual(@NotNull @RequestParam("mediaId") int mediaId){
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.manual(media);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/downloaded")
	public ModelAndView downloaded(@NotNull @RequestParam("mediaId") int mediaId) throws NotifyException{
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.downloaded(media);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/unmanual")
	public ModelAndView unmanual(@NotNull @RequestParam("mediaId") int mediaId){
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.unmanual(media);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/manually-delete")
	public ModelAndView delete(@NotNull @RequestParam("mediaId") int mediaId) throws RequestFailedException{
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.manuallyDelete(media);
		return new ModelAndView("api/success");
	}
	
	@Transactional
	@PostMapping("/refresh")
	public ModelAndView refresh(@NotNull @RequestParam("mediaId") int mediaId) throws RequestFailedException, UpdateException, NotifyException{
		var media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> new RuntimeException("Media not found"));
		mediaService.update(media, true);
		return new ModelAndView("api/success");
	}
}
