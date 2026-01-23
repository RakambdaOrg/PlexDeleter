package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class InstantAsStringWithoutNanosSerializer extends StdSerializer<Instant>{
	private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
			.appendInstant(0)
			.toFormatter();
	
	public InstantAsStringWithoutNanosSerializer(){
		super(Instant.class);
	}
	
	@Override
	public void serialize(Instant value, tools.jackson.core.JsonGenerator gen, SerializationContext provider) throws JacksonException{
		gen.writeString(FORMATTER.format(value));
	}
}
