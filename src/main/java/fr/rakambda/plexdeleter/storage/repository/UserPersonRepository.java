package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPersonRepository extends JpaRepository<UserPersonEntity, Integer>{
}
