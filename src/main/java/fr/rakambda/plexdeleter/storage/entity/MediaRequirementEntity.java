package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "MediaRequirement", schema = "PlexDeleter")
@IdClass(MediaRequirementEntity.TableId.class)
public class MediaRequirementEntity{
	@Id
	@Column(name = "MediaId", nullable = false)
	private Integer mediaId;
	@Basic
	@Column(name = "GroupId", nullable = false)
	@NotNull
	private Integer groupId;
	@Enumerated(EnumType.STRING)
	@Column(name = "Status", nullable = false)
	@NotNull
	private MediaRequirementStatus status;
	
	@Data
	public static class TableId implements Serializable{
		private Integer mediaId;
		private Integer groupId;
	}
}
