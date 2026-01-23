package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.awt.*;

public class ColorSerializer extends StdSerializer<Color>{
	public ColorSerializer(){
		super(Color.class);
	}
	
	@Override
	public void serialize(Color value, JsonGenerator gen, SerializationContext provider) throws JacksonException{
		gen.writeNumber(value.getRGB() & 0xFFFFFF);
	}
}
