package fr.rakambda.plexdeleter.service;

import org.jetbrains.annotations.NotNull;

public class UpdateException extends Exception{
	public UpdateException(@NotNull String message){
		super(message);
	}
}
