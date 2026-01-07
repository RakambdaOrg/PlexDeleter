package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrApiService;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrApiService;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponseWrapper;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.DeletionConfiguration;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("I want it to compile for now")
@ExtendWith(MockitoExtension.class)
class DeleteMediaSchedulerTest{
	private static final int PLEX_ID = 123;
	private static final int DELAY = 5;
	private static final MediaType MEDIA_TYPE = MediaType.SEASON;
	private static final int RATING_KEY = 456;
	private static final String REMOTE_PREFIX = "/remote";
	private static final String RAW_FILE_PATH = "path/to/file";
	private static final String REMOTE_FILE = REMOTE_PREFIX + "/" + RAW_FILE_PATH;
	
	@Mock
	private MediaRepository mediaRepository;
	@Mock
	private SupervisionService supervisionService;
	@Mock
	private TautulliApiService tautulliApiService;
	@Mock
	private OverseerrApiService overseerrApiService;
	@Mock
	private ApplicationConfiguration applicationConfiguration;
	@Mock
	private DeletionConfiguration deletionConfiguration;
	@Mock
	private MediaEntity mediaEntity;
	@Mock
	private TautulliResponseWrapper<GetMetadataResponse> getMetadataResponseTautulliResponseWrapper;
	@Mock
	private TautulliResponse<GetMetadataResponse> getMetadataResponseTautulliResponse;
	@Mock
	private GetMetadataResponse getMetadataResponse;
	@Mock
	private MediaInfo mediaInfo;
	@Mock
	private MediaPart mediaPart;
	@Mock
	private RadarrApiService radarrApiService;
	@Mock
	private SonarrApiService sonarrApiService;
	
	@TempDir
	private Path tempDir;
	
	private DeleteMediaScheduler tested;
	
	@BeforeEach
	void setUp() throws RequestFailedException{
		lenient().when(applicationConfiguration.getDeletion()).thenReturn(deletionConfiguration);
		lenient().when(deletionConfiguration.getDaysDelay()).thenReturn(DELAY);
		lenient().when(deletionConfiguration.getRemotePathMappings()).thenReturn(Map.of(REMOTE_PREFIX, tempDir.toString()));
		lenient().when(mediaEntity.getPlexId()).thenReturn(PLEX_ID);
		lenient().when(mediaEntity.getType()).thenReturn(MEDIA_TYPE);
		lenient().when(tautulliApiService.getElementsRatingKeys(PLEX_ID, MEDIA_TYPE)).thenReturn(List.of(RATING_KEY));
		lenient().when(tautulliApiService.getMetadata(RATING_KEY)).thenReturn(getMetadataResponseTautulliResponseWrapper);
		lenient().when(getMetadataResponseTautulliResponseWrapper.getResponse()).thenReturn(getMetadataResponseTautulliResponse);
		lenient().when(getMetadataResponseTautulliResponse.getData()).thenReturn(getMetadataResponse);
		lenient().when(getMetadataResponse.getAddedAt()).thenReturn(Instant.parse("2024-01-01T00:00:00.000Z"));
		lenient().when(getMetadataResponse.getMediaInfo()).thenReturn(Set.of(mediaInfo));
		lenient().when(mediaInfo.getParts()).thenReturn(Set.of(mediaPart));
		lenient().when(mediaPart.getFile()).thenReturn(REMOTE_FILE);

		tested = new DeleteMediaScheduler(mediaRepository, supervisionService, tautulliApiService, applicationConfiguration, overseerrApiService, radarrApiService, sonarrApiService);
	}
	
	@Test
	void skipWhenNotInPlex(){
		when(mediaEntity.getPlexId()).thenReturn(null);
		
		assertThatThrownBy(() -> tested.delete(mediaEntity))
				.hasMessage("Cannot delete media mediaEntity as it does not seem to be in Plex/Tautulli");
	}
	
	@Test
	void skipWhenNoRatingKey() throws RequestFailedException{
		when(tautulliApiService.getElementsRatingKeys(PLEX_ID, MEDIA_TYPE)).thenReturn(List.of());
		
		assertThatThrownBy(() -> tested.delete(mediaEntity))
				.hasMessage("Could not find metadata & files for mediaEntity");
	}
	
	@Test
	void skipIfTooRecent() throws DeleteMediaScheduler.DeletionException, RequestFailedException, IOException{
		when(getMetadataResponse.getAddedAt()).thenReturn(Instant.now());
		
		assertThat(tested.delete(mediaEntity)).isZero();
		verify(mediaEntity, never()).setStatus(MediaStatus.DELETED);
	}
	
	@Test
	void deleteMedia() throws DeleteMediaScheduler.DeletionException, RequestFailedException, IOException{
		var remoteFile = "%d/%d/%s".formatted(System.nanoTime(), System.nanoTime(), "file");
		var paths = createLocalFile(tempDir, remoteFile);
		
		when(mediaPart.getFile()).thenReturn(REMOTE_PREFIX + "/" + remoteFile);
		
		tested.delete(mediaEntity);
		
		verify(mediaEntity).setStatus(MediaStatus.DELETED);
		
		assertThat(paths).allSatisfy(path -> assertThat(path).doesNotExist());
		assertThat(tempDir).isEmptyDirectory();
	}
	
	@Test
	void deleteMediaWithSidecar() throws DeleteMediaScheduler.DeletionException, RequestFailedException, IOException{
		var runaName = System.nanoTime();
		var remoteFile = "%d/%d/%s".formatted(runaName, runaName, "file");
		var remoteFileNfo = "%d/%d/%s.nfo".formatted(runaName, runaName, "file");
		var paths = createLocalFile(tempDir, remoteFile, remoteFileNfo);
		
		when(mediaPart.getFile()).thenReturn(REMOTE_PREFIX + "/" + remoteFile);
		
		tested.delete(mediaEntity);
		
		verify(mediaEntity).setStatus(MediaStatus.DELETED);
		
		assertThat(paths).allSatisfy(path -> assertThat(path).doesNotExist());
		assertThat(tempDir).isEmptyDirectory();
	}
	
	private List<Path> createLocalFile(Path tempDir, String... paths) throws IOException{
		var createdPaths = new LinkedList<Path>();
		for(var path : paths){
			var localFile = tempDir.resolve(path);
			Files.createDirectories(localFile.getParent());
			Files.createFile(localFile);
			createdPaths.add(localFile);
		}
		return createdPaths;
	}
}