package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.MediaType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding(Attachment.class)
public class Attachment{
	@JsonProperty
	private int id;
	@JsonProperty
	@NonNull
	private String filename;
	@JsonProperty
	private String description;
	@JsonProperty("content_type")
	private String contentType;
	@JsonProperty
	private Integer size;
	
	@JsonIgnore
	private byte[] data;
	@JsonIgnore
	private MediaType mediaType;
}
