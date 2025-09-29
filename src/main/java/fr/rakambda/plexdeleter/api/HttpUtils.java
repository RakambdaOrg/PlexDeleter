package fr.rakambda.plexdeleter.api;

import io.netty.handler.logging.LogLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils{
	@Nullable
	public static <T> T unwrapIfStatusOk(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity.getBody();
	}
	
	@NonNull
	public static <T> T unwrapIfStatusOkAndNotNullBody(@NonNull ResponseEntity<T> entity) throws RequestFailedException{
		return requireNotNullBody(requireStatusOk(entity)).getBody();
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireStatusOk(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireStatusOkOrNotFound(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful() && entity.getStatusCode().value() != 404){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireNotNullBody(@NonNull ResponseEntity<T> entity) throws RequestFailedException{
		if(Objects.isNull(entity.getBody())){
			throw new RequestFailedException("No body received");
		}
		return entity;
	}
	
	@NonNull
	public static ClientHttpConnector wiretapClientConnector(@NonNull Class<?> clazz){
		return new ReactorClientHttpConnector(HttpClient.create().wiretap(clazz.getCanonicalName(), LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL));
	}
	
	@NonNull
	public static ExchangeFilterFunction logErrorFilter(){
		return logErrorFilter(Set.of());
	}
	
	@NonNull
	public static ExchangeFilterFunction logErrorFilter(@NonNull Collection<HttpStatusCode> ignoreStatuses){
		return ExchangeFilterFunction.ofResponseProcessor(response -> {
			if(response.statusCode().isError() && !ignoreStatuses.contains(response.statusCode())){
				return response.bodyToMono(String.class)
						.flatMap(body -> {
							log.error("Request {} {} failed with status code {} and body {}",
									response.request().getMethod(),
									response.request().getURI(),
									response.statusCode(),
									body
							);
							return Mono.just(response);
						});
			}
			return Mono.just(response);
		});
	}
	
	@NonNull
	public static ExchangeFilterFunction retryOnStatus(@NonNull Collection<HttpStatusCode> statuses){
		return retryOnStatus(statuses, 100, ChronoUnit.MILLIS, 60_000);
	}
	
	@NonNull
	public static ExchangeFilterFunction retryOnStatus(@NonNull Collection<HttpStatusCode> statuses, @NonNull TemporalUnit unit, int defaultDelay){
		return retryOnStatus(statuses, 100, unit, defaultDelay);
	}
	
	@NonNull
	public static ExchangeFilterFunction retryOnStatus(@NonNull Collection<HttpStatusCode> statuses, int max, @NonNull TemporalUnit unit, int defaultDelay){
		return (request, next) -> next.exchange(request)
				.retryWhen(Retry.max(max)
						.filter(err -> err instanceof WebClientResponseException webClientResponseException && statuses.contains(webClientResponseException.getStatusCode()))
						.doBeforeRetryAsync(signal -> Mono.delay(calculateDelay(signal.failure(), unit, defaultDelay)).then()));
	}
	
	@NonNull
	public static Duration calculateDelay(@NonNull Throwable failure, @NonNull TemporalUnit unit, int defaultDelay){
		if(!(failure instanceof WebClientResponseException webClientResponseException)){
			return Duration.of(defaultDelay, unit);
		}
		
		var retryAfterHeader = webClientResponseException.getHeaders().getFirst("Retry-After");
		var retryAfter = Duration.of(Optional.ofNullable(retryAfterHeader)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.orElse(defaultDelay), unit);
		
		log.warn("Retry later for request on {}: {}", Optional.ofNullable(webClientResponseException.getRequest()).map(HttpRequest::getURI).orElse(null), retryAfter);
		return retryAfter;
	}
}
