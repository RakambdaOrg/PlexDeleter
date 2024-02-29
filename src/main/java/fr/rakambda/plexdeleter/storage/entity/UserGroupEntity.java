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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "UserGroup", schema = "PlexDeleter")
public class UserGroupEntity{
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;
	@Basic
	@Column(name = "Name", nullable = false)
	@NotNull
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(name = "NotificationType", nullable = false)
	@NotNull
	private NotificationType type;
	@Basic
	@Column(name = "NotificationValue", nullable = false)
	@NotNull
	private String value;
	@Basic
	@Column(name = "Locale")
	@Nullable
	private String locale;
	@Basic
	@Column(name = "LastNotification")
	@Nullable
	private Instant lastNotification;
	@Basic
	@Column(name = "Display", nullable = false)
	@NotNull
	private Boolean display;
	@Basic
	@Column(name = "ServarrTag")
	@Nullable
	private String servarrTag;
}
