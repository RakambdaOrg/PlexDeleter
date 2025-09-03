package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaRequirementRepository extends JpaRepository<MediaRequirementEntity, MediaRequirementEntity.TableId>{
	@NonNull
	List<MediaRequirementEntity> findAllByStatusIs(@NonNull MediaRequirementStatus status);
	
	@NonNull
	List<MediaRequirementEntity> findAllByIdGroupIdAndStatusIs(int groupId, @NonNull MediaRequirementStatus status);
}
