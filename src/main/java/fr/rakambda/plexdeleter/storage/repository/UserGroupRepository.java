package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Integer>{
	@NotNull
	List<UserGroupEntity> findAllByLastNotificationBefore(@NotNull Instant before);
}
