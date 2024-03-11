package fr.rakambda.plexdeleter.api.discord;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.data.DiscordResponse;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
	
	public DiscordWebhookService(){
		apiClient = WebClient.builder().build();
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
					.body(BodyInserters.fromValue(message))
					.retrieve()
					.toEntity(DiscordResponse.class)
					.retryWhen(Retry.indefinitely()
							.filter(WebClientResponseException.TooManyRequests.class::isInstance)
							.doBeforeRetryAsync(signal -> Mono.delay(calculateDelay(signal.failure())).then()))
					.blockOptional()
					.orElseThrow(() -> new RequestFailedException("Failed to send discord webhook message %s".formatted(message))));
		}
		finally{
			lock.release();
		}
	}
	
	@NotNull
	private static Duration calculateDelay(@NotNull Throwable failure){
		var headers = ((WebClientResponseException.ServiceUnavailable) failure).getHeaders();
		
		var retryAfter = Duration.ofMillis(Optional.ofNullable(headers.getFirst("Retry-After"))
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.orElse(60000));
		
		log.warn("Discord webhook retry later : {}", retryAfter);
		return retryAfter;
	}
}
