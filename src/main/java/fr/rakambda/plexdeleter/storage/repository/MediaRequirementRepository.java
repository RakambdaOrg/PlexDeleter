package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaRequirementRepository extends JpaRepository<MediaRequirementEntity, MediaRequirementEntity.TableId>{
	@NotNull
	List<MediaRequirementEntity> findAllByStatusIs(@NotNull MediaRequirementStatus status);
	
	@NotNull
	List<MediaRequirementEntity> findAllByIdGroupIdAndStatusIs(int groupId, @NotNull MediaRequirementStatus status);
}
