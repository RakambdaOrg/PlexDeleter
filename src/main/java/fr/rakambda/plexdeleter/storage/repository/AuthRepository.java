package fr.rakambda.plexdeleter.storage.repository;

import fr.rakambda.plexdeleter.storage.entity.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<AuthEntity, AuthEntity.TableId>{
}
