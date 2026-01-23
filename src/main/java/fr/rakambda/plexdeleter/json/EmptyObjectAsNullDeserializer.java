package fr.rakambda.plexdeleter.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;

public class EmptyObjectAsNullDeserializer<T> extends StdDeserializer<T>{
	private final Class<T> valueClass;
	private final JsonMapper defaultMapper;
	
	public EmptyObjectAsNullDeserializer(){
		this((Class<T>) Object.class);
	}
	
	protected EmptyObjectAsNullDeserializer(Class<T> t){
		super(t);
		this.valueClass = t;
		defaultMapper = JsonMapper.builder()
				.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.findAndAddModules()
				.build();
	}
	
	@Override
	public ValueDeserializer<?> createContextual(tools.jackson.databind.DeserializationContext ctxt, tools.jackson.databind.BeanProperty property){
		return new EmptyObjectAsNullDeserializer<>(property.getType().getRawClass());
	}
	
	@Override
	public T deserialize(JsonParser p, tools.jackson.databind.DeserializationContext ctxt) throws JacksonException{
		var tree = p.readValueAsTree();
		if(!tree.isObject()){
			return (T) getNullValue(ctxt);
		}
		
		var hasNoFields = tree.propertyNames().isEmpty();
		if(hasNoFields){
			return (T) getNullValue(ctxt);
		}
		
		return defaultMapper.treeToValue(tree, valueClass);
	}
}
