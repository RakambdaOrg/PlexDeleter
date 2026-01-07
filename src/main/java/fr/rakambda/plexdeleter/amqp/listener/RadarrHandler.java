package fr.rakambda.plexdeleter.amqp.listener;

import fr.rakambda.plexdeleter.amqp.AmqpConstants;
import fr.rakambda.plexdeleter.amqp.RabbitService;
import fr.rakambda.plexdeleter.amqp.message.RadarrMessage;
import fr.rakambda.plexdeleter.service.RadarrService;
import fr.rakambda.plexdeleter.web.webhook.radarr.data.Movie;
import fr.rakambda.plexdeleter.web.webhook.radarr.data.RadarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RadarrHandler extends RetryMessageHandler<RadarrMessage>{
	private final RadarrService radarrService;
	private final AmqpConstants amqpConstants;
	
	public RadarrHandler(RadarrService radarrService, RabbitService rabbitService, AmqpConstants amqpConstants){
		super(rabbitService, amqpConstants);
		this.radarrService = radarrService;
		this.amqpConstants = amqpConstants;
	}
	
	@Override
	protected String getRoutingKey(){
		return amqpConstants.ROUTING_KEY_PROCESS_RADARR;
	}
	
	@Transactional
	@RabbitListener(queues = "#{amqpConfiguration.prefixed(amqpConstants.QUEUE_PROCESS_RADARR)}")
	public void receive(@NonNull RadarrMessage message, @Headers Map<String, Object> headers){
		handle(message, headers, this::handleMessage);
	}
	
	private void handleMessage(@NonNull RadarrMessage message){
		log.info("New AMQP Radarr message received {}", message);
		
		var webhookData = new RadarrWebhook(
				message.getType(),
				new Movie(message.getMovieId(), message.getMovieTitle(), message.getMovieTmdbId())
		);
		
		switch(message.getType()){
			case "Grab" -> radarrService.onMovieGrabbed(webhookData);
		}
	}
	
	@Override
	protected long getRetryDelay(){
		return TimeUnit.HOURS.toMillis(1);
	}
}
