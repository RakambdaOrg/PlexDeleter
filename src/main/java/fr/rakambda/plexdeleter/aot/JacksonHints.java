package fr.rakambda.plexdeleter.aot;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JacksonHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NotNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		var cl = Optional.ofNullable(classLoader).orElseGet(ClassLoader::getSystemClassLoader);
		
		registerAll(hints, cl,
				"fr.rakambda.plexdeleter.api.discord.data",
				"fr.rakambda.plexdeleter.api.overseerr.data",
				"fr.rakambda.plexdeleter.api.plex.data",
				"fr.rakambda.plexdeleter.api.servarr.data",
				"fr.rakambda.plexdeleter.api.servarr.radarr.data",
				"fr.rakambda.plexdeleter.api.servarr.sonarr.data",
				"fr.rakambda.plexdeleter.api.tautulli.data"
		);
	}
	
	private void registerAll(@NotNull RuntimeHints hints, @NotNull ClassLoader classLoader, @NotNull String... packageNames){
		for(var packageName : packageNames){
			register(hints, classLoader, packageName);
		}
	}
	
	private void register(@NotNull RuntimeHints hints, @NotNull ClassLoader classLoader, @NotNull String packageName){
		var klasses = findAllClassesUsingClassLoader(classLoader, packageName);
		for(var klass : klasses){
			log.info("Registering Jackson hint for {}", klass);
			hints.reflection()
					.registerType(klass,
							MemberCategory.DECLARED_FIELDS,
							MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
							MemberCategory.INVOKE_DECLARED_METHODS,
							MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS
					);
		}
	}
	
	@NotNull
	public Set<Class<?>> findAllClassesUsingClassLoader(@NotNull ClassLoader classLoader, @NotNull String packageName){
		try(var is = classLoader.getResourceAsStream(packageName.replaceAll("[.]", "/"));
				var isr = new InputStreamReader(is);
				var reader = new BufferedReader(isr)){
			
			return reader.lines()
					.filter(line -> line.endsWith(".class"))
					.map(line -> getClass(line, packageName))
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		}
		catch(IOException e){
			log.error("Failed to list classes in {}", packageName, e);
			return Set.of();
		}
	}
	
	@Nullable
	private Class<?> getClass(@NotNull String className, @NotNull String packageName){
		try{
			return Class.forName("%s.%s".formatted(packageName, className.substring(0, className.lastIndexOf('.'))));
		}
		catch(ClassNotFoundException e){
			// handle the exception
		}
		return null;
	}
}
