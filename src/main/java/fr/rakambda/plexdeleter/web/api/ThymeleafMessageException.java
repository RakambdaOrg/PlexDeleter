package fr.rakambda.plexdeleter.web.api;

import lombok.Getter;

@Getter
public class ThymeleafMessageException extends Exception{
	private final String expression;
	
	public ThymeleafMessageException(String message, String expression){
		super(message);
		this.expression = expression;
	}
}
