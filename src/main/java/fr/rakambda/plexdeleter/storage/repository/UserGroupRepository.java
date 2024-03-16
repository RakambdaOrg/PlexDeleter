package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Integer>{
	@NotNull
	List<UserGroupEntity> findAllByLastNotificationBefore(@NotNull Instant before);
	
	@Query(value = """
			SELECT DISTINCT G
			FROM UserGroupEntity G
			INNER JOIN UserPersonEntity P ON P.groupId = G.id
			WHERE P.plexId = ?1"""
	)
	@NotNull
	Optional<UserGroupEntity> findByContainingPlexUserId(int plexUserId);
	
	@Query(value = """
			SELECT G
			FROM UserGroupEntity G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			INNER JOIN MediaEntity M ON M.id = MR.media.id
			WHERE M.overseerrId = ?1 AND M.index = ?2"""
	)
	List<UserGroupEntity> findAllByHasRequirementOn(int overseerrId, int index);
	
	@Query(value = """
			SELECT G
			FROM UserGroupEntity G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			WHERE MR.media.id = ?1 AND MR.status = ?2"""
	)
	List<UserGroupEntity> findAllByHasRequirementOnOverseerr(int overseerrId, @NotNull MediaRequirementStatus status);
	
	@Query(value = """
			SELECT G
			FROM UserGroupEntity G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id
			WHERE MR.media.plexId = ?1 AND MR.status = ?2"""
	)
	List<UserGroupEntity> findAllByHasRequirementOnPlex(int ratingKey, @NotNull MediaRequirementStatus status);
	
	@Query(value = """
			SELECT DISTINCT G
			FROM UserGroupEntity G
			INNER JOIN UserGroupLibraryEntity UGL ON UGL.userGroup = G
			INNER JOIN MediaRequirementEntity MR ON MR.group.id = G.id AND MR.media.plexId = ?1
			WHERE MR.media.plexId <> ?1 AND UGL.name = ?2"""
	)
	Collection<UserGroupEntity> findAllByDoesNotHaveRequirementOnPlexAndInterestedInLibrary(int ratingKey, @NotNull String libraryName);
}
