package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.service.UserService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.thymeleaf.expression.Lists;
import java.util.List;

@Slf4j
public class ThymeleafHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NotNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		try{
			hints.reflection().registerMethod(UserService.class.getMethod("getSoonDeletedMedias"), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(UserService.class.getMethod("getMediaOverseerrUrl", MediaEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(UserService.class.getMethod("getUserMedias", UserPersonEntity.class), ExecutableMode.INVOKE);
			hints.reflection().registerMethod(Lists.class.getMethod("isEmpty", List.class), ExecutableMode.INVOKE);
		}
		catch(NoSuchMethodException e){
			throw new RuntimeException(e);
		}
	}
}
