package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Integer>{
	@NonNull
	List<UserGroupEntity> findAllByLastNotificationBefore(@NonNull Instant before);
	
	@Query(value = """
			SELECT DISTINCT G
			FROM UserGroupEntity G
			INNER JOIN UserPersonEntity P ON P.groupId = G.id
			WHERE P.plexId = ?1"""
	)
	@NonNull
	Optional<UserGroupEntity> findByContainingPlexUserId(int plexUserId);
	
	@Query(value = """
			SELECT DISTINCT G
			FROM UserGroupEntity G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			INNER JOIN MediaEntity M ON M.id = MR.media.id
			WHERE M.overseerrId = ?1 AND M.index = ?2 AND MR.status NOT IN ?3"""
	)
	List<UserGroupEntity> findAllByHasRequirementOn(int overseerrId, int index, Collection<MediaRequirementStatus> excludedStatuses);
	
	@Query(value = """
			SELECT G
			FROM UserGroupEntity G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			WHERE MR.media.id = ?1 AND MR.status = ?2"""
	)
	List<UserGroupEntity> findAllByHasRequirementOnOverseerr(int overseerrId, @NonNull MediaRequirementStatus status);
	
	@Query(value = """
			SELECT DISTINCT G
			FROM UserGroupEntity G
			LEFT JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			LEFT JOIN UserGroupLibraryEntity UGL ON UGL.userGroup = G
			WHERE
			(
				MR.status = ?2
				AND (
					(MR.media.plexId IS NOT NULL AND MR.media.plexId = ?1)
					OR (MR.media.tmdbId IS NOT NULL AND MR.media.tmdbId = ?4 AND MR.media.index = ?6)
					OR (MR.media.tvdbId IS NOT NULL AND MR.media.tvdbId = ?5 AND MR.media.index = ?6)
				)
			)
			OR
			(UGL.name = ?3)"""
	)
	List<UserGroupEntity> findAllByHasRequirementOnPlex(int ratingKey, @NonNull MediaRequirementStatus status, @NonNull String libraryName, @Nullable Integer tmdbId, @Nullable Integer tvdbId, int mediaIndex);
}
