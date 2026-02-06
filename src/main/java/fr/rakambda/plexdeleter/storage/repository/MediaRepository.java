package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer>{
	@NonNull
	List<MediaEntity> findAllByStatusIn(@NonNull Collection<MediaStatus> availability);
	
	@NonNull
	Optional<MediaEntity> findByPlexId(int plexId);
	
	@NonNull
	List<MediaEntity> findAllByOverseerrId(int tvdbId);
	
	@NonNull
	Optional<MediaEntity> findByOverseerrIdAndIndex(int overseerrId, int index);
	
	@NonNull
	Optional<MediaEntity> findByOverseerrIdAndIndexAndSubIndex(int overseerrId, int index, int episode);
	
	@NonNull
	Optional<MediaEntity> findByTmdbIdAndIndex(int tmdbId, int index);
	
	@NonNull
	Optional<MediaEntity> findByTvdbIdAndIndex(int tvdbId, int index);
	
	@NonNull
	Optional<MediaEntity> findByServarrIdAndIndexAndType(int servarrId, int index, @NonNull MediaType type);
	
	@NonNull
	List<MediaEntity> findAllByServarrIdAndType(int servarrId, @NonNull MediaType type);
	
	@NonNull
	Optional<MediaEntity> findByPlexGuidAndIndex(String guid, int index);
	
	long countByServarrIdAndType(int servarrId, @NonNull MediaType type);
	
	@NonNull
	List<MediaEntity> findAllByPlexGuid(String guid);
}
