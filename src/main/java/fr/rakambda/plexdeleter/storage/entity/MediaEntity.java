package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Media", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class MediaEntity{
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "Id", nullable = false)
	@ToString.Include
	private Integer id;
	@Enumerated(EnumType.STRING)
	@Column(name = "Type", nullable = false)
	@NotNull
	@ToString.Include
	private MediaType type;
	@Basic
	@Column(name = "PlexId")
	@Nullable
	private Integer plexId;
	@Basic
	@Column(name = "OverseerrId")
	@Nullable
	private Integer overseerrId;
	@Basic
	@Column(name = "ServarrId")
	@Nullable
	private Integer servarrId;
	@Basic
	@Column(name = "Name", nullable = false)
	@NotNull
	@ToString.Include
	private String name;
	@Basic
	@Column(name = "MediaIndex")
	@NotNull
	@ToString.Include
	private Integer index;
	@Basic
	@Column(name = "PartsCount", nullable = false)
	@NotNull
	private Integer partsCount;
	@Basic
	@Column(name = "AvailablePartsCount", nullable = false)
	@NotNull
	private Integer availablePartsCount;
	@Enumerated(EnumType.STRING)
	@Column(name = "Availability", nullable = false)
	@NotNull
	private MediaAvailability availability;
	@Enumerated(EnumType.STRING)
	@Column(name = "ActionStatus", nullable = false)
	@NotNull
	private MediaActionStatus actionStatus;
}
