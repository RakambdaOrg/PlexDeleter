package fr.rakambda.plexdeleter.aot;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JacksonHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		var cl = Optional.ofNullable(classLoader).orElseGet(ClassLoader::getSystemClassLoader);
		
		registerAll(hints, cl,
				"fr.rakambda.plexdeleter.json",
				"fr.rakambda.plexdeleter.api.discord.data",
				"fr.rakambda.plexdeleter.api.overseerr.data",
				"fr.rakambda.plexdeleter.api.plex.data",
				"fr.rakambda.plexdeleter.api.servarr.data",
				"fr.rakambda.plexdeleter.api.servarr.radarr.data",
				"fr.rakambda.plexdeleter.api.servarr.sonarr.data",
				"fr.rakambda.plexdeleter.api.tautulli.data",
				"fr.rakambda.plexdeleter.api.tmdb.data",
				"fr.rakambda.plexdeleter.api.tvdb.data"
		);
		
		register(hints, HashSet.class);
	}
	
	private void registerAll(@NonNull RuntimeHints hints, @NonNull ClassLoader classLoader, @NonNull String... packageNames){
		for(var packageName : packageNames){
			register(hints, classLoader, packageName);
		}
	}
	
	private void register(@NonNull RuntimeHints hints, @NonNull ClassLoader classLoader, @NonNull String packageName){
		var klasses = findAllClassesUsingClassLoader(classLoader, packageName);
		for(var klass : klasses){
			register(hints, klass);
		}
	}
	
	private void register(@NonNull RuntimeHints hints, @NonNull Class<?> klass){
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
	
	@NonNull
	public Set<Class<?>> findAllClassesUsingClassLoader(@NonNull ClassLoader classLoader, @NonNull String packageName){
		try(var is = classLoader.getResourceAsStream(packageName.replaceAll("[.]", "/"));
				var isr = new InputStreamReader(Objects.requireNonNull(is));
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
	private Class<?> getClass(@NonNull String className, @NonNull String packageName){
		try{
			return Class.forName("%s.%s".formatted(packageName, className.substring(0, className.lastIndexOf('.'))));
		}
		catch(ClassNotFoundException e){
			// handle the exception
		}
		return null;
	}
}
