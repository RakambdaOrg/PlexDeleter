package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.deser.std.StdDeserializer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CommaDelimitedStringToListDeserializer extends StdDeserializer<List<String>>{
	public CommaDelimitedStringToListDeserializer(){
		super(List.class);
	}
	
	@Override
	public List<String> deserialize(tools.jackson.core.JsonParser p, tools.jackson.databind.DeserializationContext ctxt) throws JacksonException{
		return Optional.ofNullable(p.getString())
				.map(a -> a.split(","))
				.stream()
				.flatMap(Arrays::stream)
				.toList();
	}
}
