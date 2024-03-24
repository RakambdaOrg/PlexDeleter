package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.io.IOException;
import java.io.Serial;

public class ColorSerializer extends StdSerializer<Color>{
	@Serial
	private static final long serialVersionUID = 2378259054702246279L;
	
	public ColorSerializer(){
		this(null);
	}
	
	public ColorSerializer(Class<Color> t){
		super(t);
	}
	
	@Override
	public void serialize(@NotNull Color color, @NotNull JsonGenerator jsonGenerator, @NotNull SerializerProvider serializerProvider) throws IOException{
		jsonGenerator.writeNumber(color.getRGB() & 0xFFFFFF);
	}
}
