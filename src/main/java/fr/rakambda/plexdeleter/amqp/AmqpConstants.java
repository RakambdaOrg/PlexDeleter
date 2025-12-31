package fr.rakambda.plexdeleter.amqp;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConstants{
	public final String EXCHANGE_PROCESS = "exchange.process";
	public final String EXCHANGE_DEAD_LETTER = "exchange.dead-letter";
	
	public final String ROUTING_KEY_DEAD_LETTER_DEFAULT = "routing.dead-letter.tautulli";
	public final String ROUTING_KEY_PROCESS_TAUTULLI = "routing.process.tautulli";
	
	public final String QUEUE_DEAD_LETTER_TAUTULLI = "queue.dead-letter.tautulli";
	public final String QUEUE_PROCESS_TAUTULLI = "queue.process.tautulli";
	
	public final String HEADER_X_DELAY = "x-delay";
	public final String HEADER_X_MESSAGE_TTL = "x-message-ttl";
	public final String HEADER_X_REDELIVER_COUNT_REMAINING = "x-redeliver-count-remaining";
	public final String HEADER_X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	public final String HEADER_X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
}
