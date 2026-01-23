package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.notify.AbstractNotificationService;
import fr.rakambda.plexdeleter.notify.LanguageInfo;
import fr.rakambda.plexdeleter.notify.context.MetadataProviderInfo;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import fr.rakambda.plexdeleter.service.data.LibraryElement;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.thymeleaf.engine.IterationStatusVar;
import org.thymeleaf.expression.Lists;
import org.thymeleaf.expression.Strings;
import org.thymeleaf.extras.springsecurity6.auth.Authorization;
import java.util.List;
import java.util.Locale;

@Slf4j
public class ThymeleafHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		try{
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaOverseerrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaRadarrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaSonarrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaPlexUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaTraktUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getRatingKeyPlexUrl", int.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getTableColorClass", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getOwnUrl"), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getAddWatchMediaUrl", int.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaTmdbUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(ThymeleafService.class.getMethod("getMediaTvdbUrl", MediaEntity.class), ExecutableMode.INVOKE);
			
			hints.reflection().registerMethod(AbstractNotificationService.class.getMethod("getEpisodes", MediaEntity.class, UserGroupEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(AbstractNotificationService.class.getMethod("getTypeKey", MediaEntity.class), ExecutableMode.INVOKE);
			
			hints.reflection().registerMethod(Locale.class.getMethod("getLanguage"), ExecutableMode.INVOKE);
			
			registerType(hints.reflection(), MetadataProviderInfo.class);
			registerType(hints.reflection(), LibraryElement.class);
			registerType(hints.reflection(), LanguageInfo.class);
			
			hints.reflection().registerMethod(Lists.class.getMethod("isEmpty", List.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Strings.class.getMethod("isEmpty", Object.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Strings.class.getMethod("listJoin", List.class, String.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Authorization.class.getMethod("expression", String.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(IterationStatusVar.class.getMethod("getIndex"), ExecutableMode.INVOKE);
			hints.reflection().registerField(IterationStatusVar.class.getDeclaredField("index"));
		}
		catch(NoSuchMethodException | NoSuchFieldException e){
			throw new RuntimeException(e);
		}
	}
	
	private void registerType(@NonNull ReflectionHints reflection, @NonNull Class<?> klass){
		reflection.registerType(klass,
				MemberCategory.ACCESS_DECLARED_FIELDS,
				MemberCategory.ACCESS_PUBLIC_FIELDS,
				MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_DECLARED_METHODS,
				MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
				MemberCategory.INVOKE_PUBLIC_METHODS
		);
	}
}
