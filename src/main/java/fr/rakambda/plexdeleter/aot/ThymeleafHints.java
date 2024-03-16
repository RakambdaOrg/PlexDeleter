package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.notify.AbstractNotificationService;
import fr.rakambda.plexdeleter.service.UserService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.thymeleaf.engine.IterationStatusVar;
import org.thymeleaf.expression.Lists;
import org.thymeleaf.expression.Strings;
import java.util.List;

@Slf4j
public class ThymeleafHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NotNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		try{
			hints.reflection().registerMethod(UserService.class.getMethod("getMediaOverseerrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(UserService.class.getMethod("getMediaRadarrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(UserService.class.getMethod("getMediaSonarrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(AbstractNotificationService.class.getMethod("getEpisodes", MediaEntity.class, UserGroupEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(AbstractNotificationService.class.getMethod("getTypeKey", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Lists.class.getMethod("isEmpty", List.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Strings.class.getMethod("isEmpty", Object.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Strings.class.getMethod("listJoin", List.class, String.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(IterationStatusVar.class.getMethod("getIndex"), ExecutableMode.INVOKE);
			hints.reflection().registerField(IterationStatusVar.class.getDeclaredField("index"));
		}
		catch(NoSuchMethodException | NoSuchFieldException e){
			throw new RuntimeException(e);
		}
	}
}
