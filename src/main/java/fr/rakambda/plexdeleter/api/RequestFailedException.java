package fr.rakambda.plexdeleter.api;

public class RequestFailedException extends ApiException{
	public RequestFailedException(){
		super("Request failed");
	}
	
	public RequestFailedException(String message){
		super(message);
	}
	
	public RequestFailedException(String message, Throwable cause){
		super(message, cause);
	}
}
