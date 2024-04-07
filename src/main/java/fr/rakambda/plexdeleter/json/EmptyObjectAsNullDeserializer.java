package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.Serial;

public class EmptyObjectAsNullDeserializer<T> extends StdDeserializer<T> implements ContextualDeserializer{
	@Serial
	private static final long serialVersionUID = -931343925587134946L;
	
	private final Class<T> valueClass;
	private final ObjectMapper defaultMapper;
	
	public EmptyObjectAsNullDeserializer(){
		this(null);
	}
	
	protected EmptyObjectAsNullDeserializer(Class<T> t){
		super(t);
		this.valueClass = t;
		defaultMapper = JsonMapper.builder()
				.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.build()
				.findAndRegisterModules();
	}
	
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property){
		return new EmptyObjectAsNullDeserializer<>(property.getType().getRawClass());
	}
	
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
		var tree = p.readValueAsTree();
		if(!tree.isObject()){
			return getNullValue(ctxt);
		}
		
		var hasFields = tree.fieldNames().hasNext();
		if(!hasFields){
			return getNullValue(ctxt);
		}
		
		return defaultMapper.treeToValue(tree, valueClass);
	}
}
