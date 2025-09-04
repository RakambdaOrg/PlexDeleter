package fr.rakambda.plexdeleter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.transaction.support.TransactionOperations;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig{
	@Bean
	public JdbcIndexedSessionRepository sessionRepository(JdbcOperations jdbcOperations, TransactionOperations transactionOperations){
		return new JdbcIndexedSessionRepository(jdbcOperations, transactionOperations);
	}
}
