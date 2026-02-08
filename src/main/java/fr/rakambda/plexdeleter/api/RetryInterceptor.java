package fr.rakambda.plexdeleter.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RetryInterceptor implements ClientHttpRequestInterceptor{
	@NonNull
	private final RetryTemplate retryTemplate;
	@NonNull
	private final TemporalUnit retryHeaderUnit;
	@NonNull
	private final List<HttpStatusCode> statuses;
	@NonNull
	private final AtomicLong retryAfterHeader;
	
	public RetryInterceptor(long maxRetries, long backoffMs, @NonNull TemporalUnit retryHeaderUnit, @NonNull HttpStatusCode... statuses){
		this.retryHeaderUnit = retryHeaderUnit;
		this.statuses = List.of(statuses);
		this.retryAfterHeader = new AtomicLong(0);
		
		var policy = RetryPolicy.builder()
				.backOff(new DynamicRetryAfterBackOff(maxRetries, backoffMs, retryAfterHeader))
				.predicate(err -> err instanceof HttpStatusCodeException ex && this.statuses.contains(ex.getStatusCode()))
				.build();
		this.retryTemplate = new RetryTemplate(policy);
	}
	
	@NonNull
	@Override
	public ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException{
		try{
			return retryTemplate.execute(() -> {
				try{
					var response = execution.execute(request, body);
					
					if(statuses.contains(response.getStatusCode())){
						calculateDelay(request, response).ifPresent(d -> retryAfterHeader.set(d.toMillis()));
						throw new HttpServerErrorException(response.getStatusCode(), "Retryable error code");
					}
					return response;
				}
				catch(WebClientResponseException e){
					if(statuses.contains(e.getStatusCode())){
						calculateDelay(e).ifPresent(d -> retryAfterHeader.set(d.toMillis()));
					}
					throw e;
				}
			});
		}
		catch(RetryException e){
			throw new RuntimeException("Failed retrying request", e);
		}
	}
	
	@NonNull
	private Optional<Duration> calculateDelay(@NonNull WebClientResponseException failure){
		var retryAfterHeader = failure.getHeaders().getFirst("Retry-After");
		var retryAfter = Optional.ofNullable(retryAfterHeader)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.map(v -> Duration.of(v, retryHeaderUnit));
		
		log.debug("Retry later for request on {}: {}", Optional.ofNullable(failure.getRequest()).map(HttpRequest::getURI).orElse(null), retryAfter);
		return retryAfter;
	}
	
	@NonNull
	private Optional<Duration> calculateDelay(@NonNull HttpRequest request, @NonNull ClientHttpResponse response){
		var retryAfterHeader = response.getHeaders().getFirst("Retry-After");
		var retryAfter = Optional.ofNullable(retryAfterHeader)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.map(v -> Duration.of(v, retryHeaderUnit));
		
		log.debug("Retry later for request on {}: {}", Optional.of(request).map(HttpRequest::getURI).orElse(null), retryAfter);
		return retryAfter;
	}
	
	@RequiredArgsConstructor
	private static class DynamicRetryAfterBackOff implements BackOff{
		private final long maxRetries;
		private final long defaultBackoffMs;
		@NonNull
		private final AtomicLong retryAfterHeader;
		
		@Override
		public BackOffExecution start(){
			return new DynamicRetryAfterBackOffExecution(maxRetries, defaultBackoffMs, retryAfterHeader);
		}
	}
	
	@RequiredArgsConstructor
	private static class DynamicRetryAfterBackOffExecution implements BackOffExecution{
		private final long maxRetries;
		private final long defaultBackoffMs;
		@NonNull
		private final AtomicLong retryAfterHeader;
		
		private long currentAttempts = 0;
		
		@Override
		public long nextBackOff(){
			this.currentAttempts++;
			if(this.currentAttempts > maxRetries){
				return STOP;
			}
			
			var retryHeader = retryAfterHeader.get();
			return retryHeader > 0 ? retryHeader : this.defaultBackoffMs;
		}
	}
}
