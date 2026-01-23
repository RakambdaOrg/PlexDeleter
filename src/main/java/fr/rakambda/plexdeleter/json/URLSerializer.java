package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.net.URL;

public class URLSerializer extends StdSerializer<URL>{
	public URLSerializer(){
		super(URL.class);
	}
	
	@Override
	public void serialize(URL value, tools.jackson.core.JsonGenerator gen, SerializationContext provider) throws JacksonException{
		gen.writeString(value.toString());
	}
}
