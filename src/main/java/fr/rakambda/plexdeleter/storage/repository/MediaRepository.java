package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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
	List<MediaEntity> findAllByAvailability(@NotNull MediaAvailability availability);
	
	@NotNull
	Optional<MediaEntity> findByPlexId(int plexId);
	
	@NotNull
	List<MediaEntity> findAllByOverseerrIdAndAvailability(int tvdbId, @NotNull MediaAvailability availability);
	
	@NotNull
	Optional<MediaEntity> findByOverseerrIdAndIndex(int overseerrId, int index);
	
	@NotNull
	Optional<MediaEntity> findByServarrIdAndIndex(int servarrId, int index);
}
