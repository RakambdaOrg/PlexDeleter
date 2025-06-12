package fr.rakambda.plexdeleter.amqp;

import fr.rakambda.plexdeleter.amqp.message.IAmqpMessage;
import fr.rakambda.plexdeleter.amqp.message.TautulliMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

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
	
	public void sendMessage(@NotNull IAmqpMessage message){
		sendMessage(message, null, Duration.ZERO);
	}
	
	public void sendMessage(@NotNull IAmqpMessage message, @Nullable Integer retry, @NotNull Duration delay){
		var delayMs = delay.toMillis();
		var retryCount = Optional.ofNullable(retry);
		switch(message){
			case TautulliMessage m -> sendMessage(amqpConstants.EXCHANGE_PROCESS, amqpConstants.ROUTING_KEY_PROCESS_TAUTULLI, retryCount.orElse(10), delayMs, m);
		}
	}
	
	public void sendMessage(@NotNull String exchange, @NotNull String key, int retryCount, long delay, @NotNull IAmqpMessage message){
		log.info("Sending message {}", message);
		rabbitTemplate.convertAndSend(amqpConfiguration.prefixed(exchange), key, message, getMessagePostProcessor(retryCount, delay));
	}
	
	public void sendMessage(@NotNull String exchange, @NotNull String key, int retryCount, long delay, @NotNull Object message){
		log.info("Sending message {} => {} with {} retries and delay of {} ms : {}", exchange, key, retryCount, delay, message);
		rabbitTemplate.convertAndSend(amqpConfiguration.prefixed(exchange), key, message, getMessagePostProcessor(retryCount, delay));
	}
	
	public void sendDeadLetter(@NotNull Object message){
		log.info("Sending event message {}", message);
		rabbitTemplate.convertAndSend(amqpConfiguration.prefixed(amqpConstants.EXCHANGE_DEAD_LETTER), amqpConstants.ROUTING_KEY_DEAD_LETTER_DEFAULT, message);
	}
	
	@NotNull
	private MessagePostProcessor getMessagePostProcessor(int retryCount, long delay){
		return m -> {
			m.getMessageProperties().setHeader(amqpConstants.HEADER_X_DELAY, Math.min(Integer.MAX_VALUE, delay));
			m.getMessageProperties().setHeader(amqpConstants.HEADER_X_REDELIVER_COUNT_REMAINING, retryCount);
			return m;
		};
	}
}
