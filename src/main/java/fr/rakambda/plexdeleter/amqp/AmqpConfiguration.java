package fr.rakambda.plexdeleter.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import java.util.Map;

@Configuration
@EnableRabbit
public class AmqpConfiguration{
	private final AmqpConstants amqpConstants;
	private final ApplicationConfiguration applicationConfiguration;
	
	@Autowired
	public AmqpConfiguration(AmqpConstants amqpConstants, ApplicationConfiguration applicationConfiguration){
		this.amqpConstants = amqpConstants;
		this.applicationConfiguration = applicationConfiguration;
	}
	
	@Bean
	@Qualifier("exchangeProcess")
	public CustomExchange exchangeProcess(){
		return new CustomExchange(prefixed(amqpConstants.EXCHANGE_PROCESS), "x-delayed-message", true, false,
				Map.of("x-delayed-type", "direct"));
	}
	
	@Bean
	@Qualifier("exchangeDeadLetter")
	public DirectExchange exchangeDeadLetter(){
		return ExchangeBuilder.directExchange(prefixed(amqpConstants.EXCHANGE_DEAD_LETTER)).durable(true).build();
	}
	
	@Bean
	@Qualifier("queueProcessTautulli")
	public Queue queueProcessTautulli(){
		return QueueBuilder.durable(prefixed(amqpConstants.QUEUE_PROCESS_TAUTULLI))
				.withArgument(amqpConstants.HEADER_X_DEAD_LETTER_EXCHANGE, prefixed(amqpConstants.EXCHANGE_DEAD_LETTER))
				.withArgument(amqpConstants.HEADER_X_DEAD_LETTER_ROUTING_KEY, amqpConstants.ROUTING_KEY_DEAD_LETTER_DEFAULT)
				.build();
	}
	
	@Bean
	@Qualifier("queueDeadLetter")
	public Queue queueDeadLetter(){
		return QueueBuilder.durable(prefixed(amqpConstants.QUEUE_DEAD_LETTER)).build();
	}
	
	@Bean
	@Qualifier("bindingProcessTautulli")
	public Binding bindingProcessTautulli(@Qualifier("queueProcessTautulli") Queue queue, @Qualifier("exchangeProcess") CustomExchange exchange){
		return BindingBuilder.bind(queue).to(exchange).with(amqpConstants.ROUTING_KEY_PROCESS_TAUTULLI).noargs();
	}
	
	@Bean
	@Qualifier("bindingDeadLetter")
	public Binding bindingDeadLetter(@Qualifier("queueDeadLetter") Queue queue, @Qualifier("exchangeDeadLetter") DirectExchange exchange){
		return BindingBuilder.bind(queue).to(exchange).with(amqpConstants.ROUTING_KEY_DEAD_LETTER_DEFAULT);
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter){
		var backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(1000);
		backOffPolicy.setMultiplier(10);
		backOffPolicy.setMaxInterval(60000);
		
		var retryTemplate = new RetryTemplate();
		retryTemplate.setBackOffPolicy(backOffPolicy);
		
		var rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		rabbitTemplate.setRetryTemplate(retryTemplate);
		return rabbitTemplate;
	}
	
	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter(ObjectMapper jsonObjectMapper){
		return new Jackson2JsonMessageConverter(jsonObjectMapper);
	}
	
	@NotNull
	public String prefixed(@NotNull String name){
		return applicationConfiguration.getAmqp().getPrefix() + "." + name;
	}
}
