package fr.rakambda.plexdeleter.amqp.listener;

import fr.rakambda.plexdeleter.ThrowingRunnable;
import fr.rakambda.plexdeleter.amqp.AmqpConstants;
import fr.rakambda.plexdeleter.amqp.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class RetryMessageHandler<M>{
	private final RabbitService rabbitService;
	private final AmqpConstants amqpConstants;
	
	public RetryMessageHandler(RabbitService rabbitService, AmqpConstants amqpConstants){
		this.rabbitService = rabbitService;
		this.amqpConstants = amqpConstants;
	}
	
	protected String getExchange(){
		return amqpConstants.EXCHANGE_PROCESS;
	}
	
	protected abstract String getRoutingKey();
	
	protected void handle(@NotNull M message, @NotNull Map<String, Object> headers, @NotNull ThrowingRunnable<M> handler){
		try{
			handler.run(message);
		}
		catch(Throwable e){
			handleError(e, headers, message);
		}
	}
	
	protected void handle(@NotNull List<Message<M>> messages, @NotNull ThrowingRunnable<List<Message<M>>> handler){
		try{
			handler.run(messages);
		}
		catch(Throwable e){
			handleErrors(e, messages);
		}
	}
	
	protected void handleErrors(@NotNull Throwable throwable, @NotNull List<Message<M>> messages){
		messages.forEach(m -> handleError(throwable, m.getHeaders(), m.getPayload()));
	}
	
	protected void handleError(@NotNull Throwable throwable, @NotNull Map<String, Object> headers, @NotNull M message){
		log.warn("Failed to handle message {}", message, throwable);
		
		var retry = Optional.ofNullable(headers.get(amqpConstants.HEADER_X_REDELIVER_COUNT_REMAINING))
				.filter(Integer.class::isInstance)
				.map(Integer.class::cast)
				.orElse(0);
		
		var newRetry = retry - 1;
		
		if(retry < 0){
			log.warn("Retry count for message is {}, not retrying", retry);
			handleNoRetry(throwable, message);
			return;
		}
		
		log.warn("Retrying message with retry count {} (was {})", newRetry, retry);
		rabbitService.sendMessage(getExchange(), getRoutingKey(), newRetry, 30000, message);
	}
	
	protected void handleNoRetry(@NotNull Throwable throwable, @NotNull M message){
		rabbitService.sendDeadLetter(message);
	}
}
