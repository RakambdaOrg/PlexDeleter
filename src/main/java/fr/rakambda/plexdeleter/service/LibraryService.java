package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetLibraryMediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponse;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.service.data.LibraryElement;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
	private final ApplicationConfiguration applicationConfiguration;
	private final TautulliService tautulliService;
	private final MediaRepository mediaRepository;
	
	@Autowired
	public LibraryService(ApplicationConfiguration applicationConfiguration, TautulliService tautulliService, MediaRepository mediaRepository){
		this.applicationConfiguration = applicationConfiguration;
		this.tautulliService = tautulliService;
		this.mediaRepository = mediaRepository;
	}
	
	@NotNull
	public Collection<LibraryElement> getAllLibraryContentWithoutMedia(){
		var libraryElements = Optional.ofNullable(applicationConfiguration.getPlex().getTemporaryLibraries())
				.stream()
				.flatMap(Collection::stream)
				.flatMap(this::getLibraryContent)
				.toList();
		
		return libraryElements.stream()
				.filter(this::isRecordMissing)
				.sorted(Comparator.comparing(LibraryElement::type)
						.thenComparing(LibraryElement::name))
				.toList();
	}
	
	private boolean isRecordMissing(@NotNull LibraryElement element){
		return !mediaRepository.existsByRootPlexId(element.ratingKey());
	}
	
	@NotNull
	private Stream<LibraryElement> getLibraryContent(int section){
		try{
			return Optional.ofNullable(tautulliService.getLibraryMediaInfo(section).getResponse())
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
