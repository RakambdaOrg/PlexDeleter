package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.aot.JacksonHints;
import fr.rakambda.plexdeleter.aot.ResourceHints;
import fr.rakambda.plexdeleter.aot.ThymeleafHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EntityScan("fr.rakambda.plexdeleter.storage")
@EnableJpaRepositories("fr.rakambda.plexdeleter.storage")
@SpringBootApplication(scanBasePackages = "fr.rakambda.plexdeleter")
@ImportRuntimeHints({
		JacksonHints.class,
		ResourceHints.class,
		ThymeleafHints.class
})
public class PlexDeleterApplication{
	public static void main(String[] args){
		SpringApplication.run(PlexDeleterApplication.class, args);
	}
}
