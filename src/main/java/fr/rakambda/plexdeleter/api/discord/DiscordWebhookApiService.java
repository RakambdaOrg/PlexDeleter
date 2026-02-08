package fr.rakambda.plexdeleter.api.discord;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.RetryInterceptor;
import fr.rakambda.plexdeleter.api.discord.data.DiscordResponse;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import static java.lang.Integer.MAX_VALUE;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Slf4j
@Service
public class DiscordWebhookApiService{
	private final RestClient apiClient;
	private final Map<String, Semaphore> locks;
	
	public DiscordWebhookApiService(){
		apiClient = RestClient.builder()
				.requestInterceptor(new RetryInterceptor(MAX_VALUE, 60_000, MILLIS, TOO_MANY_REQUESTS))
				.build();
		locks = new ConcurrentHashMap<>();
	}
	
	@NonNull
	public DiscordResponse sendWebhookMessage(@NonNull String url, @NonNull WebhookMessage message) throws InterruptedException, RequestFailedException{
		return sendWebhookMessage(url, null, message);
	}
	
	@NonNull
	public DiscordResponse sendWebhookMessage(@NonNull String url, @Nullable Long threadId, @NonNull WebhookMessage message) throws InterruptedException, RequestFailedException{
		var lock = locks.computeIfAbsent(url, key -> new Semaphore(1));
		lock.acquire();
		try{
			if(!message.getAttachments().isEmpty()){
				log.info("Sending webhook message to discord as multipart");
				
				var multipart = new MultipartBodyBuilder();
				multipart.part("payload_json", message, MediaType.APPLICATION_JSON);
				message.getAttachments().forEach(attachment -> multipart
						.part("files[%d]".formatted(attachment.getId()), new ByteArrayResource(attachment.getData()), attachment.getMediaType())
						.filename(attachment.getFilename()));
				
				return sendWebhookMessageAs(url, threadId, message, MediaType.MULTIPART_FORM_DATA, BodyInserters.fromMultipartData(multipart.build()));
			}
			
			log.info("Sending webhook message to discord as JSON payload");
			return sendWebhookMessageAs(url, threadId, message, MediaType.APPLICATION_JSON, BodyInserters.fromValue(message));
		}
		catch(UnsupportedMediaTypeException e){
			log.info("Supported media types: {}", e.getSupportedMediaTypes());
			throw e;
		}
		finally{
			lock.release();
		}
	}
	
	@NonNull
	private DiscordResponse sendWebhookMessageAs(@NonNull String url, @Nullable Long threadId, @NonNull WebhookMessage message, @NonNull MediaType mediaType, @NonNull BodyInserter<?, ? super ClientHttpRequest> bodyInserter) throws RequestFailedException{
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.post()
				.uri(url, b -> {
					b = b.queryParam("wait", true);
					if(Objects.nonNull(threadId)){
						b.queryParam("thread_id", threadId);
					}
					return b.build();
				})
				.contentType(mediaType)
				.body(bodyInserter)
				.retrieve()
				.toEntity(DiscordResponse.class));
	}
}
