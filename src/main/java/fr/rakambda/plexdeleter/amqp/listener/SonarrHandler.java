package fr.rakambda.plexdeleter.amqp.listener;

import fr.rakambda.plexdeleter.amqp.AmqpConstants;
import fr.rakambda.plexdeleter.amqp.RabbitService;
import fr.rakambda.plexdeleter.amqp.message.SonarrMessage;
import fr.rakambda.plexdeleter.service.SonarrService;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.Episode;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.Series;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.SonarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SonarrHandler extends RetryMessageHandler<SonarrMessage>{
	private final SonarrService sonarrService;
	private final AmqpConstants amqpConstants;
	
	public SonarrHandler(SonarrService sonarrService, RabbitService rabbitService, AmqpConstants amqpConstants){
		super(rabbitService, amqpConstants);
		this.sonarrService = sonarrService;
		this.amqpConstants = amqpConstants;
	}
	
	@Override
	protected String getRoutingKey(){
		return amqpConstants.ROUTING_KEY_PROCESS_SONARR;
	}
	
	@Transactional
	@RabbitListener(queues = "#{amqpConfiguration.prefixed(amqpConstants.QUEUE_PROCESS_SONARR)}")
	public void receive(@NonNull SonarrMessage message, @Headers Map<String, Object> headers){
		handle(message, headers, this::handleMessage);
	}
	
	private void handleMessage(@NonNull SonarrMessage message){
		log.info("New AMQP Sonarr message received {}", message);
		
		var webhookData = new SonarrWebhook(
				message.getType(),
				new Series(message.getSeriesId(), message.getSeriesTitle(), message.getSeriesTvdbId()),
				Optional.ofNullable(message.getEpisodeEpisodes()).orElseGet(List::of).stream()
						.map(Integer::parseInt)
						.map(i -> new Episode(message.getEpisodeSeason(), i))
						.toList()
		);
		
		switch(message.getType()){
			case "Grab" -> sonarrService.onEpisodeGrabbed(webhookData);
		}
	}
	
	@Override
	protected long getRetryDelay(){
		return TimeUnit.HOURS.toMillis(1);
	}
}
