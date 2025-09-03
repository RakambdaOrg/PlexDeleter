package fr.rakambda.plexdeleter.api;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatusCode;

public class StatusCodeException extends RequestFailedException{
	public StatusCodeException(@NonNull HttpStatusCode status){
		super("Invalid status code " + status);
	}
}
