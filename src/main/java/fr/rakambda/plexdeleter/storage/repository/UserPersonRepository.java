package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPersonRepository extends JpaRepository<UserPersonEntity, Integer>{
	@NotNull
	Optional<UserPersonEntity> findByPlexId(int plexId);
}
