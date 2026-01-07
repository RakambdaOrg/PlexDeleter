package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CommaDelimitedStringToListDeserializer extends StdDeserializer<List<String>>{
	public CommaDelimitedStringToListDeserializer(){
		this(null);
	}
	
	protected CommaDelimitedStringToListDeserializer(Class<String> t){
		super(t);
	}
	
	@Override
	public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
		return Optional.ofNullable(p.getText())
				.map(a -> a.split(","))
				.stream()
				.flatMap(Arrays::stream)
				.toList();
	}
}
