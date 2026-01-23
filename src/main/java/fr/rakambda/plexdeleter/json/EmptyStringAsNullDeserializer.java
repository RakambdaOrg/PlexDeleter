package fr.rakambda.plexdeleter.json;

import org.springframework.util.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class EmptyStringAsNullDeserializer extends StdDeserializer<String>{
	public EmptyStringAsNullDeserializer(){
		super(String.class);
	}
	
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt){
		var value = p.getValueAsString();
		return StringUtils.hasText(value) ? value : (String) getNullValue(ctxt);
	}
}
