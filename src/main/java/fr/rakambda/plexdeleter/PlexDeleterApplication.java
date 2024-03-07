package fr.rakambda.plexdeleter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EntityScan("fr.rakambda.plexdeleter.storage")
@EnableJpaRepositories("fr.rakambda.plexdeleter.storage")
@SpringBootApplication(scanBasePackages = "fr.rakambda.plexdeleter")
public class PlexDeleterApplication{
	public static void main(String[] args){
		SpringApplication.run(PlexDeleterApplication.class, args);
	}
}
