package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletionConfiguration{
	private int daysDelay = 5;
	@NotNull
	@NonNull
	private Map<String, String> remotePathMappings = new HashMap<>();
}
