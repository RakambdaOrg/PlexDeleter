package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter.Value;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.Nulls.AS_EMPTY;
import static tools.jackson.core.StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
import static tools.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static tools.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static tools.jackson.databind.cfg.EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;

@Configuration
public class JacksonConfiguration{
	@Bean
	public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer(){
		return builder -> builder
				.enable(ORDER_MAP_ENTRIES_BY_KEYS)
				.enable(SORT_PROPERTIES_ALPHABETICALLY)
				.enable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
				.enable(ALLOW_JAVA_COMMENTS)
				.enable(ACCEPT_CASE_INSENSITIVE_ENUMS)
				.enable(ALLOW_TRAILING_COMMA)
				.enable(INCLUDE_SOURCE_IN_LOCATION)
				.changeDefaultVisibility(vc -> vc
						.withVisibility(PropertyAccessor.FIELD, ANY)
						.withVisibility(PropertyAccessor.GETTER, NONE)
						.withVisibility(PropertyAccessor.SETTER, NONE)
						.withVisibility(PropertyAccessor.CREATOR, NONE)
				)
				.changeDefaultPropertyInclusion(ic -> ic.withValueInclusion(Include.NON_NULL))
				.withConfigOverride(List.class, c -> c.setNullHandling(Value.forValueNulls(AS_EMPTY)))
				.withConfigOverride(Set.class, c -> c.setNullHandling(Value.forValueNulls(AS_EMPTY)))
				.withConfigOverride(Map.class, c -> c.setNullHandling(Value.forValueNulls(AS_EMPTY)));
	}
}
