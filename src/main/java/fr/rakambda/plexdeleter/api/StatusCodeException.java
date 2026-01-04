package fr.rakambda.plexdeleter.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatusCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatusCodeException extends RequestFailedException{
	@NonNull
	private final HttpStatusCode status;
	
	public StatusCodeException(@NonNull HttpStatusCode status){
		super("Invalid status code " + status);
		this.status = status;
	}
}
