package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmqpConfiguration{
	@NonNull
	private String prefix;
	@NonNull
	private Duration requeueDelay;
}
