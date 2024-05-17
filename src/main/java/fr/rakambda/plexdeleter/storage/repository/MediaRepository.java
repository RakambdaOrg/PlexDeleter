package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaActionStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer>{
	@Query(value = """
			SELECT M
			FROM MediaEntity M
			INNER JOIN MediaRequirementEntity MR ON M.id = MR.id.mediaId
			WHERE M.actionStatus = 'TO_DELETE'
			AND M.availability = 'DOWNLOADED'
			GROUP BY M.id
			HAVING SUM(CASE WHEN (MR.status = 'WATCHED' OR MR.status = 'ABANDONED') THEN 0 ELSE 1 END) <= 0"""
	)
	@NotNull
	List<MediaEntity> findAllReadyToDelete();
	
	@NotNull
	List<MediaEntity> findAllByAvailabilityIn(@NotNull Collection<MediaAvailability> availability);
	
	@NotNull
	List<MediaEntity> findAllByActionStatusIn(@NotNull Collection<MediaActionStatus> statuses);
	
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
