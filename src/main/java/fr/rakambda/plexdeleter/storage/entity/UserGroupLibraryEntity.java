package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_group_library", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class UserGroupLibraryEntity{
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(nullable = false)
	@ToString.Include
	private Integer id;
	@Basic
	@Column(nullable = false)
	@NonNull
	@ToString.Include
	private String name;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "groupId", referencedColumnName = "id", updatable = false, nullable = false)
	private UserGroupEntity userGroup;
}
