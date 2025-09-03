package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPersonRepository extends JpaRepository<UserPersonEntity, Integer>{
	@NonNull
	Optional<UserPersonEntity> findByPlexId(int plexId);
}
