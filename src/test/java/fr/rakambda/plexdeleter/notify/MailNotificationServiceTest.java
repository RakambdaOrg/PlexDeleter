package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tmdb.TmdbApiService;
import fr.rakambda.plexdeleter.api.tvdb.TvdbApiService;
import fr.rakambda.plexdeleter.notify.context.CompositeMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TmdbMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TraktMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TvdbMediaMetadataContext;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.io.UnsupportedEncodingException;
import java.util.List;

@ActiveProfiles("local")
@SpringBootTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class MailNotificationServiceTest{
	@Autowired
	private MailNotificationService mailNotificationService;
	@Autowired
	private UserGroupRepository userGroupRepository;
	@Autowired
	private MediaRepository mediaRepository;
	@Autowired
	private TautulliApiService tautulliApiService;
	@Autowired
	private TmdbApiService tmdbApiService;
	@Autowired
	private TvdbApiService tvdbApiService;
	
	@Test
	@Transactional
	void sendTestWatchlistMail() throws MessagingException, UnsupportedEncodingException{
		var userGroup = userGroupRepository.findById(2).orElseThrow();
		mailNotificationService.notifyWatchlist(userGroup.getNotification(), userGroup, userGroup.getRequirements());
	}
	
	@Test
	@Transactional
	void sendTestRequirementAddedMail() throws MessagingException, UnsupportedEncodingException{
		var userGroup = userGroupRepository.findById(2).orElseThrow();
		var media = userGroup.getRequirements().get(0).getMedia();
		mailNotificationService.notifyRequirementAdded(userGroup.getNotification(), userGroup, media);
	}
	
	@Test
	@Transactional
	void sendTestMediaDeletedMail() throws MessagingException, UnsupportedEncodingException{
		var userGroup = userGroupRepository.findById(2).orElseThrow();
		var media = userGroup.getRequirements().get(0).getMedia();
		mailNotificationService.notifyMediaDeleted(userGroup.getNotification(), userGroup, media);
	}
	
	@Test
	@Transactional
	void sendTestMediaAddedMail() throws MessagingException, UnsupportedEncodingException, RequestFailedException{
		var userGroup = userGroupRepository.findById(2).orElseThrow();
		var media = mediaRepository.findByOverseerrIdAndIndex(95480, 4).get();
		
		var metadata = tautulliApiService.getMetadata(834236).getResponse().getData();
		
		var tmdbMediaMetadataContext = new TmdbMediaMetadataContext(tautulliApiService, metadata, tmdbApiService);
		var tvdbMediaMetadataContext = new TvdbMediaMetadataContext(tautulliApiService, metadata, tvdbApiService);
		var traktMediaMetadataContext = new TraktMediaMetadataContext(tautulliApiService, metadata);
		var mediaMetadataContext = new CompositeMediaMetadataContext(tautulliApiService, metadata, List.of(
				tmdbMediaMetadataContext,
				tvdbMediaMetadataContext,
				traktMediaMetadataContext
		));
		mailNotificationService.notifyMediaAdded(userGroup.getNotification(), userGroup, media, mediaMetadataContext);
	}
}