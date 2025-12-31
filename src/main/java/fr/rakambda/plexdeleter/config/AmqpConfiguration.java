package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmqpConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String prefix;
	@NonNull
	@org.jspecify.annotations.NonNull
	private Duration requeueDelay;
}
