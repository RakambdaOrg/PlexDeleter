package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "media_requirement", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class MediaRequirementEntity{
	@EmbeddedId
	@ToString.Include
	private TableId id;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private MediaRequirementStatus status;
	
	@ManyToOne
	@JoinColumn(name = "mediaId", referencedColumnName = "id", updatable = false, nullable = false)
	@MapsId("media_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private MediaEntity media;
	@ManyToOne
	@JoinColumn(name = "groupId", referencedColumnName = "id", updatable = false, nullable = false)
	@MapsId("group_id")
	private UserGroupEntity group;
	
	@Embeddable
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TableId implements Serializable{
		@Column(nullable = false)
		@NotNull
		private Integer mediaId;
		@Column(nullable = false)
		@NotNull
		private Integer groupId;
	}
}
