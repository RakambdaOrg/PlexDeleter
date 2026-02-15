package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.aot.AmqpHints;
import fr.rakambda.plexdeleter.aot.HibernateHints;
import fr.rakambda.plexdeleter.aot.JacksonHints;
import fr.rakambda.plexdeleter.aot.ResourceHints;
import fr.rakambda.plexdeleter.aot.ThymeleafHints;
import fr.rakambda.plexdeleter.aot.WebHints;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import fr.rakambda.plexdeleter.config.SeerrConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import fr.rakambda.plexdeleter.config.TautulliConfiguration;
import fr.rakambda.plexdeleter.config.TmdbConfiguration;
import fr.rakambda.plexdeleter.config.TvdbConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EntityScan("fr.rakambda.plexdeleter.storage")
@EnableJpaRepositories("fr.rakambda.plexdeleter.storage")
@SpringBootApplication(scanBasePackages = "fr.rakambda.plexdeleter")
@ImportRuntimeHints({
		AmqpHints.class,
		HibernateHints.class,
		JacksonHints.class,
		ResourceHints.class,
		ThymeleafHints.class,
		WebHints.class
})
@EnableConfigurationProperties({
		SeerrConfiguration.class,
		PlexConfiguration.class,
		RadarrConfiguration.class,
		SonarrConfiguration.class,
		TautulliConfiguration.class,
		TvdbConfiguration.class,
		TmdbConfiguration.class
})
public class PlexDeleterApplication{
	public static void main(String[] args){
		SpringApplication.run(PlexDeleterApplication.class, args);
	}
}
