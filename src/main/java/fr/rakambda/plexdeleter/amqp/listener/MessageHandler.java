package fr.rakambda.plexdeleter.amqp.listener;

import fr.rakambda.plexdeleter.amqp.AmqpConstants;
import fr.rakambda.plexdeleter.amqp.RabbitService;
import fr.rakambda.plexdeleter.amqp.message.TautulliMessage;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.TautulliService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import fr.rakambda.plexdeleter.web.webhook.tautulli.data.TautulliWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class MessageHandler extends RetryMessageHandler<TautulliMessage>{
	private final TautulliService tautulliService;
	private final AmqpConstants amqpConstants;
	
	public MessageHandler(TautulliService tautulliService, RabbitService rabbitService, AmqpConstants amqpConstants){
		super(rabbitService, amqpConstants);
		this.tautulliService = tautulliService;
		this.amqpConstants = amqpConstants;
	}
	
	@Override
	protected String getRoutingKey(){
		return amqpConstants.ROUTING_KEY_PROCESS_TAUTULLI;
	}
	
	@RabbitListener(queues = "#{amqpConfiguration.prefixed(amqpConstants.QUEUE_PROCESS_TAUTULLI)}")
	public void receive(@NotNull TautulliMessage message, @Headers Map<String, Object> headers){
		handle(message, headers, this::handleMessage);
	}
	
	private void handleMessage(@NotNull TautulliMessage message) throws ThymeleafMessageException, RequestFailedException, UpdateException, NotifyException, IOException{
		log.info("New AMQP Tautulli message received {}", message);
		
		if(!message.getMediaType().isNotifyAdded()){
			return;
		}
		
		var webhookData = new TautulliWebhook(
				message.getType(),
				message.getMediaType(),
				message.getUserId(),
				message.getRatingKey(),
				message.getParentRatingKey(),
				message.getGrandparentRatingKey(),
				message.getTvdbId(),
				message.getTmdbId()
		);
		
		switch(message.getType()){
			case "watched" -> {
				tautulliService.updateRequirement(webhookData);
				tautulliService.addNewMediaIfPreviousExist(webhookData);
			}
			case "added" -> {
				tautulliService.updateMedia(webhookData);
				tautulliService.notifyMedia(webhookData);
				tautulliService.addNewMediaIfPreviousExist(webhookData);
			}
		}
	}
}
