package fr.rakambda.plexdeleter.api.plex.gql;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.gql.data.request.GraphQlRequest;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityData;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityWatchHistory;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.GqlError;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.GqlResponse;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.PagedData;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.service.GraphQlService;
import fr.rakambda.plexdeleter.service.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlexCommunityService{
	private final GraphQlService graphQlService;
	private final WebClient apiClient;
	
	@Autowired
	public PlexCommunityService(GraphQlService graphQlService, ApplicationConfiguration applicationConfiguration){
		this.graphQlService = graphQlService;
		
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getPlex().getCommunityEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.defaultHeader("X-Plex-Token", applicationConfiguration.getPlex().getCommunityToken())
				.filter(HttpUtils.logErrorFilter())
				.build();
	}
	
	@NonNull
	public List<ActivityData> listActivityForItem(@NonNull String metadataId, @Nullable Instant since) throws RequestFailedException{
		log.info("Getting Plex activity for item {}", metadataId);
		var params = Map.<String, Object> of(
				"first", 100,
				"metadataID", metadataId
		);
		Predicate<List<ActivityData>> halter = Objects.isNull(since) ? null : (entries) -> entries.stream().anyMatch(e -> {
			if(!(e instanceof ActivityWatchHistory activityWatchHistory)){
				return false;
			}
			return activityWatchHistory.getDate().isBefore(since);
		});
		return this.pagedGqlQuery("api/plex/community/gql/GetActivityFeedForItem.gql", params, new ParameterizedTypeReference<>(){}, null, halter);
	}
	
	@NonNull
	private <T> List<T> pagedGqlQuery(@NonNull String definition, @NonNull Map<String, Object> variables, @NonNull ParameterizedTypeReference<GqlResponse<PagedData<T>>> type, @Nullable Integer maxPagesInput, @Nullable Predicate<List<T>> halter) throws RequestFailedException{
		var elements = new LinkedList<T>();
		var currentPage = 1;
		var maxPages = Objects.isNull(maxPagesInput) ? Integer.MAX_VALUE : maxPagesInput;
		var hasNextPage = false;
		String nexCursor = null;
		
		do{
			if(currentPage > 1){
				try{
					Thread.sleep(1000);
				}
				catch(InterruptedException e){
					throw new RuntimeException("Failed to sleep while querying multiple pages on Plex Community API", e);
				}
			}
			var pagedVariables = new HashMap<>(variables);
			if(Objects.nonNull(nexCursor)){
				pagedVariables.put("after", nexCursor);
			}
			
			var response = this.gqlQuery(definition, pagedVariables, type);
			var newElements = response.getQuery().getNodes();
			elements.addAll(newElements);
			currentPage++;
			nexCursor = response.getQuery().getPageInfo().getEndCursor();
			hasNextPage = response.getQuery().getPageInfo().isHasNextPage();
			if(Objects.nonNull(halter) && halter.test(elements)){
				break;
			}
		}
		while(hasNextPage && currentPage <= maxPages);
		
		return elements;
	}
	
	@NonNull
	private <T> T gqlQuery(@NonNull String definition, @NonNull Map<String, Object> variables, @NonNull ParameterizedTypeReference<GqlResponse<T>> type) throws RequestFailedException{
		try{
			log.debug("Sending gql query from definition {}", definition);
			
			var gqlQuery = GraphQlRequest.builder()
					.query(graphQlService.readQuery(definition))
					.variables(variables)
					.build();
			
			log.info("Sending gql query {}", gqlQuery);
			
			var response = HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(gqlQuery))
					.retrieve()
					.toEntity(type)
					.retryWhen(Retry.indefinitely()
							.filter(WebClientResponseException.TooManyRequests.class::isInstance)
							.doBeforeRetryAsync(signal -> Mono.delay(calculateDelay(signal.failure())).then()))
					.blockOptional()
					.orElseThrow(RequestFailedException::new));
			
			if(response.getErrors().isEmpty() && Objects.nonNull(response.getData())){
				return response.getData();
			}
			
			throw new RequestFailedException(response.getErrors().stream().map(GqlError::getMessage).collect(Collectors.joining(" | ")));
		}
		catch(ParseException e){
			throw new RequestFailedException("Failed to construct request", e);
		}
	}
	
	@NonNull
	private static Duration calculateDelay(@NonNull Throwable failure){
		if(!(failure instanceof WebClientResponseException.TooManyRequests webClientResponseException)){
			return Duration.ofMinutes(1);
		}
		
		var retryAfterHeader = webClientResponseException.getHeaders().getFirst("Retry-After");
		var retryAfter = Duration.ofSeconds(Optional.ofNullable(retryAfterHeader)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.orElse(60));
		
		log.warn("Anilist graphql api retry later : {}", retryAfter);
		return retryAfter;
	}
}
