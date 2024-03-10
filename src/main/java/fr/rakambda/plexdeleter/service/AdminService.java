package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService{
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public AdminService(MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository){
		this.mediaRepository = mediaRepository;
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@NotNull
	public List<MediaEntity> getMediasThatCanBeDeleted(){
		return mediaRepository.findAllByAvailability(MediaAvailability.DOWNLOADING).stream()
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME)
				.toList();
	}
	
	@NotNull
	public List<MediaRequirementEntity> getMediaRequirementsThatCanBeCompleted(){
		return mediaRequirementRepository.findAllByStatusIs(MediaRequirementStatus.WAITING).stream()
				.sorted((r1, r2) -> {
					var compareMedia = MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME.compare(r1.getMedia(), r2.getMedia());
					if(compareMedia != 0){
						return compareMedia;
					}
					return r1.getGroup().getName().compareTo(r2.getGroup().getName());
				})
				.toList();
	}
}
