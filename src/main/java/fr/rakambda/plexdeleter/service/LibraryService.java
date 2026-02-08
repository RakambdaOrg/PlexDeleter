package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetLibraryMediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponse;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.service.data.LibraryElement;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class LibraryService{
	private final PlexConfiguration plexConfiguration;
	private final TautulliApiService tautulliApiService;
	private final MediaRepository mediaRepository;
	
	@Autowired
	public LibraryService(PlexConfiguration plexConfiguration, TautulliApiService tautulliApiService, MediaRepository mediaRepository){
		this.plexConfiguration = plexConfiguration;
		this.tautulliApiService = tautulliApiService;
		this.mediaRepository = mediaRepository;
	}
	
	@NonNull
	public Collection<GetMetadataResponse> getAllLibraryContentWithoutMedia(){
		var libraryElements = Optional.ofNullable(plexConfiguration.temporaryLibraries())
				.stream()
				.flatMap(Collection::stream)
				.flatMap(this::getLibraryContent)
				.map(this::getLibraryElementDetails)
				.flatMap(Optional::stream)
				.toList();
		
		return libraryElements.stream()
				.filter(this::isRecordMissing)
				.sorted(Comparator.comparing(GetMetadataResponse::getMediaType)
						.thenComparing(GetMetadataResponse::getTitle))
				.toList();
	}
	
	@NonNull
	private Optional<GetMetadataResponse> getLibraryElementDetails(@NonNull LibraryElement element){
		try{
			return tautulliApiService.getMetadata(element.ratingKey()).getResponse().getDataOptional();
		}
		catch(RequestFailedException e){
			throw new RuntimeException(e);
		}
	}
	
	private boolean isRecordMissing(@NonNull GetMetadataResponse element){
		return mediaRepository.findAllByPlexGuid(element.getGuid()).stream()
				.map(MediaEntity::getStatus)
				.noneMatch(MediaStatus::isOnDisk);
	}
	
	@NonNull
	private Stream<LibraryElement> getLibraryContent(int section){
		try{
			return Optional.ofNullable(tautulliApiService.getLibraryMediaInfo(section).getResponse())
					.map(TautulliResponse::getData)
					.map(GetLibraryMediaInfo::getData).stream()
					.flatMap(Collection::stream)
					.map(record -> new LibraryElement(
							record.getRatingKey(),
							record.getTitle(),
							switch(record.getMediaType()){
								case SHOW, EPISODE, SEASON -> MediaType.SEASON;
								case MOVIE -> MediaType.MOVIE;
								case PHOTO, TRACK, ARTIST -> null;
							}
					))
					.filter(element -> Objects.nonNull(element.type()));
		}
		catch(RequestFailedException e){
			log.error("Failed to get library content for section id {}", section, e);
			return Stream.empty();
		}
	}
}
