package fr.rakambda.plexdeleter.api;

import io.netty.handler.logging.LogLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils{
	@Nullable
	public static <T> T unwrapIfStatusOk(@NotNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity.getBody();
	}
	
	@NotNull
	public static <T> T unwrapIfStatusOkAndNotNullBody(@NotNull ResponseEntity<T> entity) throws RequestFailedException{
		return requireNotNullBody(requireStatusOk(entity)).getBody();
	}
	
	@NotNull
	public static <T> ResponseEntity<T> requireStatusOk(@NotNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NotNull
	public static <T> ResponseEntity<T> requireStatusOkOrNotFound(@NotNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful() && entity.getStatusCode().value() != 404){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NotNull
	public static <T> ResponseEntity<T> requireNotNullBody(@NotNull ResponseEntity<T> entity) throws RequestFailedException{
		if(Objects.isNull(entity.getBody())){
			throw new RequestFailedException("No body received");
		}
		return entity;
	}
	
	@NotNull
	public static ClientHttpConnector wiretapClientConnector(@NotNull Class<?> clazz){
		return new ReactorClientHttpConnector(HttpClient.create().wiretap(clazz.getCanonicalName(), LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL));
	}
	
	@NotNull
	public static ExchangeFilterFunction logErrorFilter(){
		return ExchangeFilterFunction.ofResponseProcessor(response -> {
			if(response.statusCode().isError()){
				response.bodyToMono(String.class).handle((body, sink) -> {
					log.error("Got error with status {} and body {}", response.statusCode(), body);
					sink.error(new IllegalStateException());
				});
			}
			return Mono.just(response);
		});
	}
}
