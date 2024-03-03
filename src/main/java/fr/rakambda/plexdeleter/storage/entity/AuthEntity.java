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
@Table(name = "Auth", schema = "PlexDeleter")
@IdClass(AuthEntity.TableId.class)
public class AuthEntity{
	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "Type", nullable = false)
	@NotNull
	private AuthType type;
	@Id
	@Column(name = "Username", nullable = false)
	@NotNull
	private String username;
	@Basic
	@Column(name = "Password", nullable = false)
	@NotNull
	private String password;
	
	@Data
	public static class TableId implements Serializable{
		private AuthType type;
		private String username;
	}
}
