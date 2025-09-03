package fr.rakambda.plexdeleter.aot;

import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class HibernateHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader){
		hints.reflection()
				.registerType(MediaRequirementEntity.TableId.class,
						MemberCategory.DECLARED_FIELDS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
						MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
						MemberCategory.INVOKE_PUBLIC_METHODS
				)
				.registerType(org.mariadb.jdbc.Configuration.Builder.class,
						MemberCategory.DECLARED_FIELDS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
						MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
						MemberCategory.INVOKE_PUBLIC_METHODS
				);
	}
}
