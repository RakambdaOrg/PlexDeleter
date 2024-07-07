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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "media_requirement", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class MediaRequirementEntity{
	public static final Comparator<? super MediaRequirementEntity> COMPARATOR_BY_MEDIA =
			(r1, r2) -> MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX.compare(r1.getMedia(), r2.getMedia());
	
	@EmbeddedId
	@ToString.Include
	private TableId id;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private MediaRequirementStatus status;
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private Long watchedCount;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "mediaId", referencedColumnName = "id", updatable = false, nullable = false)
	@MapsId("mediaId")
	private MediaEntity media;
	@ManyToOne(optional = false)
	@JoinColumn(name = "groupId", referencedColumnName = "id", updatable = false, nullable = false)
	@MapsId("groupId")
	private UserGroupEntity group;
	
	@Embeddable
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	public static class TableId implements Serializable{
		@Serial
		private static final long serialVersionUID = 6710822541443664746L;
		
		@Column(nullable = false)
		@NotNull
		private Integer mediaId;
		@Column(nullable = false)
		@NotNull
		private Integer groupId;
	}
}
