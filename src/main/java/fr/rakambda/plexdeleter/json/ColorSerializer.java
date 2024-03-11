package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color>{
	@Override
	public void serialize(@NotNull Color color, @NotNull JsonGenerator jsonGenerator, @NotNull SerializerProvider serializerProvider) throws IOException{
		jsonGenerator.writeNumber(color.getRGB() & 0xFFFFFF);
	}
}
