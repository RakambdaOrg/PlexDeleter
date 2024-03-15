package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "media", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class MediaEntity{
	public static final Comparator<MediaEntity> COMPARATOR_BY_TYPE_THEN_NAME = Comparator.comparing(MediaEntity::getType).thenComparing(MediaEntity::getName);
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(nullable = false)
	@ToString.Include
	private Integer id;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private MediaType type;
	@Basic
	@Column
	@Nullable
	private Integer plexId;
	@Basic
	@Column
	@Nullable
	private Integer overseerrId;
	@Basic
	@Column
	@Nullable
	private Integer servarrId;
	@Basic
	@Column
	@Nullable
	private Integer tvdbId;
	@Basic
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private String name;
	@Basic
	@Column(name = "MediaIndex")
	@NotNull
	@ToString.Include
	private Integer index;
	@Basic
	@Column(nullable = false)
	@NotNull
	private Integer partsCount;
	@Basic
	@Column(nullable = false)
	@NotNull
	private Integer availablePartsCount;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private MediaAvailability availability;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private MediaActionStatus actionStatus;
	
	@OneToMany(cascade = CascadeType.REMOVE, targetEntity = MediaRequirementEntity.class, mappedBy = "media")
	private List<MediaRequirementEntity> requirements;
	
	public boolean isCompletable(){
		return availablePartsCount >= partsCount;
	}
}
