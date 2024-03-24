package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serial;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantAsStringSerializer extends StdSerializer<Instant>{
	@Serial
	private static final long serialVersionUID = -884696594405719424L;
	
	private static final DateTimeFormatter DF = DateTimeFormatter.ISO_INSTANT;
	
	public InstantAsStringSerializer(){
		this(null);
	}
	
	public InstantAsStringSerializer(Class<Instant> t){
		super(t);
	}
	
	@Override
	public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException{
		gen.writeString(DF.format(value));
	}
}
