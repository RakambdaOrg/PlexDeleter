package fr.rakambda.plexdeleter.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils{
	@Nullable
	public static <T> T unwrapIfStatusOk(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity.getBody();
	}
	
	@NonNull
	public static <T> T unwrapIfStatusOkAndNotNullBody(@NonNull ResponseEntity<T> entity) throws RequestFailedException{
		return requireNotNullBody(requireStatusOk(entity)).getBody();
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireStatusOk(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful()){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireStatusOkOrNotFound(@NonNull ResponseEntity<T> entity) throws StatusCodeException{
		if(!entity.getStatusCode().is2xxSuccessful() && entity.getStatusCode().value() != 404){
			throw new StatusCodeException(entity.getStatusCode());
		}
		return entity;
	}
	
	@NonNull
	public static <T> ResponseEntity<T> requireNotNullBody(@NonNull ResponseEntity<T> entity) throws RequestFailedException{
		if(Objects.isNull(entity.getBody())){
			throw new RequestFailedException("No body received");
		}
		return entity;
	}
}
