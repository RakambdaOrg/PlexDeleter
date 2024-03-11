package fr.rakambda.plexdeleter.api.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.data.DiscordResponse;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
public class DiscordWebhookService{
	private final WebClient apiClient;
	private final Map<String, Semaphore> locks;
	
	@Autowired
	public DiscordWebhookService(ObjectMapper objectMapper){
		apiClient = WebClient.builder()
				.codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
						.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
				)
				.build();
		locks = new ConcurrentHashMap<>();
	}
	
	@NotNull
	public DiscordResponse sendWebhookMessage(@NotNull String url, @NotNull WebhookMessage message) throws InterruptedException, RequestFailedException{
		return sendWebhookMessage(url, null, message);
	}
	
	@NotNull
	public DiscordResponse sendWebhookMessage(@NotNull String url, @Nullable Long threadId, @NotNull WebhookMessage message) throws InterruptedException, RequestFailedException{
		var lock = locks.computeIfAbsent(url, key -> new Semaphore(1));
		lock.acquire();
		try{
			log.info("Sending webhook message to discord");
			return HttpUtils.withStatusOkAndBody(apiClient.post()
					.uri(url, b -> {
						b = b.queryParam("wait", true);
						if(Objects.nonNull(threadId)){
							b.queryParam("thread_id", threadId);
						}
						return b.build();
					})
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(message))
					.retrieve()
					.toEntity(DiscordResponse.class)
					.retryWhen(Retry.indefinitely()
							.filter(WebClientResponseException.TooManyRequests.class::isInstance)
							.doBeforeRetryAsync(signal -> Mono.delay(calculateDelay(signal.failure())).then()))
					.blockOptional()
					.orElseThrow(() -> new RequestFailedException("Failed to send discord webhook message %s".formatted(message))));
		}
		catch(UnsupportedMediaTypeException e){
			log.info("Supported media types: {}", e.getSupportedMediaTypes());
			throw e;
		}
		finally{
			lock.release();
		}
	}
	
	@NotNull
	private static Duration calculateDelay(@NotNull Throwable failure){
		if(!(failure instanceof WebClientResponseException webClientResponseException)){
			return Duration.ofMinutes(1);
		}
		
		var retryAfterHeader = webClientResponseException.getHeaders().getFirst("Retry-After");
		var retryAfter = Duration.ofMillis(Optional.ofNullable(retryAfterHeader)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.orElse(60000));
		
		log.warn("Discord webhook retry later : {}", retryAfter);
		return retryAfter;
	}
}
