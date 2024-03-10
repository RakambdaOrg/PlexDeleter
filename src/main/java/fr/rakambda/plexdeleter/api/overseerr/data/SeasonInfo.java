package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({SeasonInfo.class})
public class SeasonInfo{
	private int id;
	private int seasonNumber;
	private int status;
	private int status4k;
	private Instant createdAt;
	private Instant updatedAt;
}
