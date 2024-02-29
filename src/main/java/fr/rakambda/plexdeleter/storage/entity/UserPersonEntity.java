package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "UserPerson", schema = "PlexDeleter")
public class UserPersonEntity{
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;
	@Basic
	@Column(name = "Name", nullable = false)
	@NotNull
	private String name;
	@Basic
	@Column(name = "PlexId", nullable = false)
	@NotNull
	private Integer plexId;
}
