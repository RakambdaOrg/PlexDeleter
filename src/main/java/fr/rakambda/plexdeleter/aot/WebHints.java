package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.security.PlexAuthenticationToken;
import fr.rakambda.plexdeleter.security.PlexUser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Slf4j
public class WebHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NotNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		hints.serialization()
				.registerType(PlexAuthenticationToken.class)
				.registerType(UsernamePasswordAuthenticationToken.class)
				.registerType(PlexUser.class);
	}
}
