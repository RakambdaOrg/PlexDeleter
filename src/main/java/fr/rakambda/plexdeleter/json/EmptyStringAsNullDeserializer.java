package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.Serial;

public class EmptyStringAsNullDeserializer extends StdDeserializer<String>{
	@Serial
	private static final long serialVersionUID = 1762330827704440476L;
	
	public EmptyStringAsNullDeserializer(){
		this(null);
	}
	
	protected EmptyStringAsNullDeserializer(Class<String> t){
		super(t);
	}
	
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
		var value = p.getValueAsString();
		return StringUtils.hasText(value) ? value : getNullValue(ctxt);
	}
}
