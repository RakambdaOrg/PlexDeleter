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
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_person", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class UserPersonEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	@ToString.Include
	private Integer id;
	@Basic
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private String name;
	@Basic
	@Column(nullable = false)
	@NotNull
	private Integer plexId;
	@Basic
	@Column(nullable = false)
	@NotNull
	private Integer groupId;
}
