package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.MediaType;

@Builder
@RegisterReflectionForBinding(Attachment.class)
public record Attachment(
		@JsonProperty int id,
		@JsonProperty @NonNull String filename,
		@JsonProperty String description,
		@JsonProperty("content_type") String contentType,
		@JsonProperty Integer size,
		@JsonIgnore byte[] data,
		@JsonIgnore MediaType mediaType
){
}
