package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_group", schema = "PlexDeleter")
@ToString(onlyExplicitlyIncluded = true)
public class UserGroupEntity{
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(nullable = false)
	@ToString.Include
	private Integer id;
	@Basic
	@Column(nullable = false)
	@NotNull
	@ToString.Include
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private NotificationType notificationType;
	@Basic
	@Column(nullable = false)
	@NotNull
	private String notificationValue;
	@Basic
	@Column
	@NotNull
	private String locale;
	@Basic
	@Column
	@NotNull
	private Instant lastNotification;
	@Basic
	@Column(nullable = false)
	@NotNull
	private Boolean display;
	@Basic
	@Column
	@Nullable
	private String servarrTag;
	
	@OneToMany(targetEntity = UserPersonEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<UserPersonEntity> persons;
	@OneToMany(targetEntity = MediaRequirementEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<MediaRequirementEntity> requirements;
	@OneToMany(targetEntity = UserGroupLibraryEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<UserGroupLibraryEntity> libraries;
	
	@NotNull
	public Locale getLocaleAsObject(){
		return new Locale.Builder().setLanguage(locale).build();
	}
}
