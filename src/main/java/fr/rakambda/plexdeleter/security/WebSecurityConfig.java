package fr.rakambda.plexdeleter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{
	@Bean
	public SecurityFilterChain basicFilterChain(HttpSecurity http, PlexAuthenticationProvider plexAuthenticationProvider, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception{
		return http
				.authenticationProvider(plexAuthenticationProvider)
				.authenticationProvider(daoAuthenticationProvider)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/user/**").hasRole("USER")
						.requestMatchers("/login/**").permitAll()
						.requestMatchers("/static/**").permitAll()
						.requestMatchers("/user/**").hasRole("USER")
						.requestMatchers("/webhook/overseerr/**").hasRole("OVERSEERR")
						.requestMatchers("/webhook/radarr/**").hasRole("RADARR")
						.requestMatchers("/webhook/sonarr/**").hasRole("SONARR")
						.requestMatchers("/webhook/tautulli/**").hasRole("TAUTULLI")
						.anyRequest().hasRole("ADMIN")
				)
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(Customizer.withDefaults())
				.with(new PlexFormLoginConfigurer<>(), c -> c
						.authenticationFilter(new PlexAuthenticationFilter())
						.loginPage("/auth")
						.failureForwardUrl("/auth?error")
						.successForwardUrl("/auth/success")
						.loginProcessingUrl("/login")
						.permitAll()
				)
				.logout(LogoutConfigurer::permitAll)
				.build();
	}
	
	@Bean
	public UserDetailsManager users(DataSource dataSource){
		return new JdbcUserDetailsManager(dataSource);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService){
		var provider = new DaoAuthenticationProvider(passwordEncoder);
		provider.setUserDetailsService(userDetailsService);
		return provider;
	}
}
