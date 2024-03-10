package fr.rakambda.plexdeleter;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader){
		hints.resources().registerPattern("lang/*");
	}
}
