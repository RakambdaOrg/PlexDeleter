package fr.rakambda.plexdeleter.service;

import org.jspecify.annotations.NonNull;

public class UpdateException extends Exception{
	public UpdateException(@NonNull String message){
		super(message);
	}
}
