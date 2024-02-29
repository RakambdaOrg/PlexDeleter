package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadarrConfiguration{
	private String endpoint;
	private String apiKey;
}
