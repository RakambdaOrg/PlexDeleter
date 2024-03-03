package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, Integer>{
	@Query(value = """
			SELECT M
			FROM MediaEntity M
			INNER JOIN MediaRequirementEntity MR ON M.id = MR.mediaId
			WHERE M.actionStatus = 'TO_DELETE'
			AND M.availability = 'DOWNLOADED'
			GROUP BY M.id
			HAVING SUM(CASE WHEN (MR.status = 'WATCHED' OR MR.status = 'ABANDONED') THEN 0 ELSE 1 END) <= 0"""
	)
	@NotNull
	List<MediaEntity> findAllReadyToDelete();
	
	@NotNull
	List<MediaEntity> findAllByAvailabilityIs(@NotNull MediaAvailability availability);
}
