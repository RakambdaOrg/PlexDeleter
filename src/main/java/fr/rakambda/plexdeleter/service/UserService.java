package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserPersonRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService{
	private final String overseerrEndpoint;
	private final MediaRepository mediaRepository;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final UserPersonRepository userPersonRepository;
	
	@Autowired
	public UserService(ApplicationConfiguration applicationConfiguration, MediaRepository mediaRepository, MediaRequirementRepository mediaRequirementRepository, UserPersonRepository userPersonRepository){
		this.mediaRepository = mediaRepository;
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.userPersonRepository = userPersonRepository;
	}
	
	@NotNull
	public List<MediaEntity> getSoonDeletedMedias(){
		return mediaRepository.findAllReadyToDelete().stream()
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME)
				.toList();
	}
	
	@Nullable
	public List<MediaEntity> getUserMedias(@NotNull UserPersonEntity userPerson){
		return mediaRequirementRepository.findAllByIdGroupIdAndStatusIs(userPerson.getGroupId(), MediaRequirementStatus.WAITING).stream()
				.map(MediaRequirementEntity::getMedia)
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME)
				.toList();
	}
	
	@Nullable
	public String getMediaOverseerrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getOverseerrId())
				.map(id -> "%s/%s/%d".formatted(overseerrEndpoint, media.getType().getOverseerrType().getValue(), id))
				.orElse(null);
	}
}
