package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import static java.lang.Math.min;

public class DiscordFieldValueSerializer extends StdSerializer<String>{
	public static final int MAX_LENGTH = 1024;
	
	public DiscordFieldValueSerializer(){
		super(String.class);
	}
	
	@Override
	public void serialize(String value, tools.jackson.core.JsonGenerator gen, SerializationContext provider) throws JacksonException{
		gen.writeString(value.substring(0, min(value.length(), MAX_LENGTH)));
	}
}
