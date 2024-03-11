package fr.rakambda.plexdeleter.aot;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class ResourceHints implements RuntimeHintsRegistrar{
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader){
		hints.resources().registerPattern("lang/*");
	}
}
