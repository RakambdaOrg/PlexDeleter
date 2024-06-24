package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer>{
	@NotNull
	List<MediaEntity> findAllByStatusIn(@NotNull Collection<MediaStatus> availability);
	
	@NotNull
	Optional<MediaEntity> findByPlexId(int plexId);
	
	@NotNull
	List<MediaEntity> findAllByOverseerrId(int tvdbId);
	
	@NotNull
	Optional<MediaEntity> findByOverseerrIdAndIndex(int overseerrId, int index);
	
	@NotNull
	Optional<MediaEntity> findByTmdbIdAndIndex(int tmdbId, int index);
	
	@NotNull
	Optional<MediaEntity> findByTvdbIdAndIndex(int tvdbId, int index);
	
	@NotNull
	Optional<MediaEntity> findByServarrIdAndIndex(int servarrId, int index);
	
	@NotNull
	Optional<MediaEntity> findByRootPlexIdAndIndex(int ratingKey, int index);
	
	long countByServarrIdAndType(int servarrId, @NotNull MediaType type);
}
