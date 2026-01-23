package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantAsStringSerializer extends StdSerializer<Instant>{
	
	private static final DateTimeFormatter DF = DateTimeFormatter.ISO_INSTANT;
	
	public InstantAsStringSerializer(){
		super(Instant.class);
	}
	
	@Override
	public void serialize(Instant value, JsonGenerator gen, SerializationContext provider) throws JacksonException{
		gen.writeString(DF.format(value));
	}
}
