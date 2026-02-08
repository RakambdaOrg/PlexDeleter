package fr.rakambda.plexdeleter.api.plex.gql;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.RetryInterceptor;
import fr.rakambda.plexdeleter.api.plex.gql.data.request.GraphQlRequest;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityData;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.ActivityWatchHistory;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.GqlError;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.GqlResponse;
import fr.rakambda.plexdeleter.api.plex.gql.data.response.PagedData;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
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
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Slf4j
@Service
public class PlexCommunityApiService{
	private final GraphQlService graphQlService;
	private final RestClient apiClient;
	
	@Autowired
	public PlexCommunityApiService(GraphQlService graphQlService, PlexConfiguration plexConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		this.graphQlService = graphQlService;
		
		apiClient = RestClient.builder()
				.baseUrl(plexConfiguration.communityEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.defaultHeader("X-Plex-Token", plexConfiguration.communityToken())
				.requestInterceptor(new RetryInterceptor(10, 60_000, ChronoUnit.SECONDS, TOO_MANY_REQUESTS))
				.requestInterceptor(clientLoggerRequestInterceptor)
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
					.body(gqlQuery)
					.retrieve()
					.toEntity(type));
			
			var errors = Optional.ofNullable(response.getErrors()).orElseGet(List::of);
			if(errors.isEmpty() && Objects.nonNull(response.getData())){
				return response.getData();
			}
			
			throw new RequestFailedException(errors.stream().map(GqlError::getMessage).collect(Collectors.joining(" | ")));
		}
		catch(ParseException e){
			throw new RequestFailedException("Failed to construct request", e);
		}
	}
}
