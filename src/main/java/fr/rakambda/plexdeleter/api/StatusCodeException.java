package fr.rakambda.plexdeleter.api;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;

public class StatusCodeException extends RequestFailedException{
	public StatusCodeException(@NotNull HttpStatusCode status){
		super("Invalid status code " + status);
	}
}
