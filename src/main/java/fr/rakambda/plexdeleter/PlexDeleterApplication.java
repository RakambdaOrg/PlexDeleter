package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.schedule.DeleteMediaScheduler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaRepositories("fr.rakambda.plexdeleter.storage")
@EntityScan("fr.rakambda.plexdeleter.storage")
@SpringBootApplication(scanBasePackages = "fr.rakambda.plexdeleter")
public class PlexDeleterApplication{
	private final DeleteMediaScheduler deleteMediaScheduler;
	
	@Autowired
	public PlexDeleterApplication(DeleteMediaScheduler deleteMediaScheduler){this.deleteMediaScheduler = deleteMediaScheduler;}
	
	public static void main(String[] args){
		SpringApplication.run(PlexDeleterApplication.class, args);
	}
	
	@PostConstruct
	public void test(){
		deleteMediaScheduler.run();
	}
}
