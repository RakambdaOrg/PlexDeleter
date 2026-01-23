package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.amqp.AmqpConstants;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class AmqpHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		hints.reflection()
				.registerType(AmqpConstants.class,
						MemberCategory.ACCESS_DECLARED_FIELDS,
						MemberCategory.ACCESS_PUBLIC_FIELDS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
						MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
						MemberCategory.INVOKE_PUBLIC_METHODS
				);
	}
}
