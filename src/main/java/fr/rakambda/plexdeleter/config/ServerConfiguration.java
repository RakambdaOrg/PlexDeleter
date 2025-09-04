package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String applicationUrl;
	@NestedConfigurationProperty
	private WebAuthNConfiguration webAuthN;
}
