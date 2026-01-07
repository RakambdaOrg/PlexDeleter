package fr.rakambda.plexdeleter.amqp;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitService{
	private final RabbitTemplate rabbitTemplate;
	private final AmqpConfiguration amqpConfiguration;
	private final AmqpConstants amqpConstants;
	
	@Autowired
	public RabbitService(RabbitTemplate rabbitTemplate, AmqpConfiguration amqpConfiguration, AmqpConstants amqpConstants){
		this.rabbitTemplate = rabbitTemplate;
		this.amqpConfiguration = amqpConfiguration;
		this.amqpConstants = amqpConstants;
	}
	
	public void sendMessage(@NonNull String exchange, @NonNull String key, int retryCount, long delay, @NonNull Object message){
		log.info("Sending message {} => {} with {} retries and delay of {} ms : {}", exchange, key, retryCount, delay, message);
		rabbitTemplate.convertAndSend(amqpConfiguration.prefixed(exchange), key, message, getMessagePostProcessor(retryCount, delay));
	}
	
	public void sendDeadLetter(@NonNull Object message){
		log.info("Sending event message {}", message);
		rabbitTemplate.convertAndSend(amqpConfiguration.prefixed(amqpConstants.EXCHANGE_DEAD_LETTER), amqpConstants.ROUTING_KEY_DEAD_LETTER_TAUTULLI, message);
	}
	
	@NonNull
	private MessagePostProcessor getMessagePostProcessor(int retryCount, long delay){
		return m -> {
			m.getMessageProperties().setHeader(amqpConstants.HEADER_X_DELAY, Math.min(Integer.MAX_VALUE, delay));
			m.getMessageProperties().setHeader(amqpConstants.HEADER_X_REDELIVER_COUNT_REMAINING, retryCount);
			return m;
		};
	}
}
