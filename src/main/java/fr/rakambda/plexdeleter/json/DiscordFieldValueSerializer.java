package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serial;
import static java.lang.Math.min;

public class DiscordFieldValueSerializer extends StdSerializer<String>{
	@Serial
	private static final long serialVersionUID = 2722842435846148915L;
	
	public static final int MAX_LENGTH = 1024;
	
	public DiscordFieldValueSerializer(){
		this(null);
	}
	
	protected DiscordFieldValueSerializer(Class<String> t){
		super(t);
	}
	
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException{
		gen.writeString(value.substring(0, min(value.length(), MAX_LENGTH)));
	}
}
