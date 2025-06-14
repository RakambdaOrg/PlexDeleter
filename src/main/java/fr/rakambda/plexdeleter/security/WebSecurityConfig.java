package fr.rakambda.plexdeleter.security;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{
	private final ApplicationConfiguration configuration;
	
	@Autowired
	public WebSecurityConfig(ApplicationConfiguration configuration){
		this.configuration = configuration;
	}
	
	@Bean
	public SecurityFilterChain basicFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception{
		return http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/").authenticated()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/user/**").hasRole("USER")
						.requestMatchers("/login/**").permitAll()
						.requestMatchers("/static/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/user/**").hasRole("USER")
						.requestMatchers("/webhook/overseerr/**").hasRole("OVERSEERR")
						.requestMatchers("/webhook/radarr/**").hasRole("RADARR")
						.requestMatchers("/webhook/sonarr/**").hasRole("SONARR")
						.requestMatchers("/webhook/tautulli/**").hasRole("TAUTULLI")
						.anyRequest().hasRole("ADMIN")
				)
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(Customizer.withDefaults())
				.authenticationManager(authenticationManager)
				.with(new PlexFormLoginConfigurer<>(), c -> c
						.authenticationFilter(new PlexAuthenticationFilter())
						.defaultSuccessUrl("/auth/success", false)
						.loginPage("/auth")
						.failureForwardUrl("/auth/error")
						.loginProcessingUrl("/login")
						.permitAll()
				)
				.webAuthn(webAuthn -> webAuthn
						.rpName(configuration.getWebAuthN().getRelayingPartyName())
						.rpId(configuration.getWebAuthN().getRelayingPartyId())
						.allowedOrigins(configuration.getWebAuthN().getAllowedOrigins()))
				.logout(LogoutConfigurer::permitAll)
				.build();
	}
	
	@Bean
	public UserDetailsManager users(DataSource dataSource){
		return new JdbcUserDetailsManager(dataSource);
	}
	
	@Bean
	public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, PlexAuthenticationProvider plexAuthenticationProvider){
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(plexAuthenticationProvider, authProvider);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public JdbcPublicKeyCredentialUserEntityRepository jdbcPublicKeyCredentialRepository(JdbcOperations jdbc){
		return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
	}
	
	@Bean
	public JdbcUserCredentialRepository jdbcUserCredentialRepository(JdbcOperations jdbc){
		return new JdbcUserCredentialRepository(jdbc);
	}
}
