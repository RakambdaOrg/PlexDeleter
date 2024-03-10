package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.rakambda.plexdeleter.json.ColorSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding({Embed.class})
public class Embed{
	@JsonProperty("author")
	private Author author;
	@JsonProperty("title")
	private String title;
	@JsonProperty("url")
	private String url;
	@JsonProperty("description")
	private String description;
	@JsonProperty("color")
	@JsonSerialize(using = ColorSerializer.class)
	private Color color;
	@JsonProperty("fields")
	@Singular
	private Collection<Field> fields;
	@JsonProperty("thumbnail")
	private Image thumbnail;
	@JsonProperty("image")
	private Image image;
	@JsonProperty("footer")
	private Footer footer;
	@JsonProperty("timestamp")
	@JsonSerialize(using = InstantSerializer.class)
	private Instant timestamp;
	
	static class InstantSerializer extends StdSerializer<Instant>{
		private static final DateTimeFormatter DF = DateTimeFormatter.ISO_INSTANT;
		
		public InstantSerializer(){
			this(null);
		}
		
		public InstantSerializer(Class<Instant> t){
			super(t);
		}
		
		@Override
		public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException{
			gen.writeString(DF.format(value));
		}
	}
}
