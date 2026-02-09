package fr.rakambda.plexdeleter.storage.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
	@NonNull
	@ToString.Include
	private String name;
	@Basic
	@Column
	@NonNull
	private String locale;
	@Basic
	@Column
	@NonNull
	private Instant lastNotification;
	@Basic
	@Column
	@Nullable
	private String servarrTag;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyWatchlist;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyRequirementAdded;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyMediaAdded;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyMediaAvailable;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyMediaDeleted;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyMediaWatched;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyRequirementManuallyWatched;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean notifyRequirementManuallyAbandoned;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean appearInCollections;
	@Basic
	@Column(nullable = false)
	@NonNull
	private Boolean canViewServerTags;
	
	@OneToMany(targetEntity = UserPersonEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<UserPersonEntity> persons;
	@OneToMany(targetEntity = MediaRequirementEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<MediaRequirementEntity> requirements;
	@OneToMany(targetEntity = UserGroupLibraryEntity.class)
	@JoinColumn(name = "groupId", referencedColumnName = "id")
	private List<UserGroupLibraryEntity> libraries;
	
	@ManyToOne(targetEntity = NotificationEntity.class)
	@JoinColumn(name = "notificationId", referencedColumnName = "id")
	@Nullable
	private NotificationEntity notification;
	@ManyToOne(targetEntity = NotificationEntity.class)
	@JoinColumn(name = "notificationMediaAddedId", referencedColumnName = "id")
	@Nullable
	private NotificationEntity notificationMediaAdded;
	
	@NonNull
	public Locale getLocaleAsObject(){
		return new Locale.Builder().setLanguage(locale).build();
	}
}
