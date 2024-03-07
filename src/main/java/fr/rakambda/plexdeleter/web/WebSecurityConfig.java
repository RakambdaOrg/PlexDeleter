package fr.rakambda.plexdeleter.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
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
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		return http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/static/**").permitAll()
						.requestMatchers("/webhook/overseerr/**").hasRole("OVERSEERR")
						.requestMatchers("/webhook/radarr/**").hasRole("RADARR")
						.requestMatchers("/webhook/sonarr/**").hasRole("SONARR")
						.requestMatchers("/webhook/tautulli/**").hasRole("TAUTULLI") // TODO Update hook with basic auth
						.anyRequest().hasRole("ADMIN")
				)
				.httpBasic(basic -> basic.realmName("basic"))
				.formLogin(form -> form.defaultSuccessUrl("/", false).permitAll())
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
}
