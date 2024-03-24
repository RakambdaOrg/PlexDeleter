package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.Serial;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class InstantAsStringWithoutNanosSerializer extends StdSerializer<Instant>{
	@Serial
	private static final long serialVersionUID = -5671874800786469735L;
	
	private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
			.appendInstant(0)
			.toFormatter();
	
	public InstantAsStringWithoutNanosSerializer(){
		this(null);
	}
	
	public InstantAsStringWithoutNanosSerializer(Class<Instant> t){
		super(t);
	}
	
	@Override
	public void serialize(@NotNull Instant instant, @NotNull JsonGenerator jsonGenerator, @NotNull SerializerProvider serializerProvider) throws IOException{
		jsonGenerator.writeString(FORMATTER.format(instant));
	}
}
