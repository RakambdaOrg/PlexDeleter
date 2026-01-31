package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerConfiguration{
	@NonNull
	private String applicationUrl;
	@NestedConfigurationProperty
	private WebAuthNConfiguration webAuthN;
}
