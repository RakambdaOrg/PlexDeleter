package fr.rakambda.plexdeleter.api;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
public class ClientLoggerRequestInterceptor implements ClientHttpRequestInterceptor{
	@NonNull
	@Override
	public ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, ClientHttpRequestExecution execution) throws IOException{
		var response = execution.execute(request, body);
		return logResponse(request, response);
	}
	
	private ClientHttpResponse logResponse(HttpRequest request, ClientHttpResponse response) throws IOException{
		if(response.getStatusCode().isError()){
			byte[] responseBody = response.getBody().readAllBytes();
			log.warn("Request {} {} failed with status code {} and body {}",
					request.getMethod(),
					request.getURI(),
					response.getStatusCode(),
					Optional.of(responseBody).filter(b -> b.length > 0).map(b -> new String(b, StandardCharsets.UTF_8)).orElse("<<empty>>")
			);
			return new BufferingClientHttpResponseWrapper(response, responseBody);        // Return wrapped response to allow reading the body again
		}
		
		return response;
	}
	
	private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse{
		private final ClientHttpResponse response;
		private final byte[] body;
		
		public BufferingClientHttpResponseWrapper(@NonNull ClientHttpResponse response, byte @NonNull [] body){
			this.response = response;
			this.body = body;
		}
		
		@NonNull
		@Override
		public InputStream getBody(){
			return new ByteArrayInputStream(body);
		}
		
		@NonNull
		@Override
		public HttpStatusCode getStatusCode() throws IOException{
			return response.getStatusCode();
		}
		
		@NonNull
		@Override
		public String getStatusText() throws IOException{
			return response.getStatusText();
		}
		
		@Override
		public void close(){
			response.close();
		}
		
		@NonNull
		@Override
		public HttpHeaders getHeaders(){
			return response.getHeaders();
		}
	}
}
