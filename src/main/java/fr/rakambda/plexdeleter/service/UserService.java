package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService{
	private final String overseerrEndpoint;
	private final String radarrEndpoint;
	private final String sonarrEndpoint;
	private final MediaRequirementRepository mediaRequirementRepository;
	
	@Autowired
	public UserService(ApplicationConfiguration applicationConfiguration, MediaRequirementRepository mediaRequirementRepository){
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
		this.radarrEndpoint = applicationConfiguration.getRadarr().getEndpoint();
		this.sonarrEndpoint = applicationConfiguration.getSonarr().getEndpoint();
		this.mediaRequirementRepository = mediaRequirementRepository;
	}
	
	@Nullable
	public List<MediaRequirementEntity> getUserRequirements(@NotNull UserPersonEntity userPerson){
		return mediaRequirementRepository.findAllByIdGroupIdAndStatusIs(userPerson.getGroupId(), MediaRequirementStatus.WAITING).stream()
				.sorted(MediaRequirementEntity.COMPARATOR_BY_MEDIA)
				.toList();
	}
	
	@Nullable
	public String getMediaOverseerrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getOverseerrId())
				.map(id -> "%s/%s/%d".formatted(overseerrEndpoint, media.getType().getOverseerrType().getValue(), id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaSonarrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getSonarrSlug())
				.map(id -> "%s/series/%s".formatted(sonarrEndpoint, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaRadarrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getRadarrSlug())
				.map(id -> "%s/movie/%s".formatted(radarrEndpoint, id))
				.orElse(null);
	}
}
