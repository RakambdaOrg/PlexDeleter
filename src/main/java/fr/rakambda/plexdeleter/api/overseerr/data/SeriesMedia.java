package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Optional;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RegisterReflectionForBinding(SeriesMedia.class)
public class SeriesMedia extends Media{
	String name;
	String originalName;
	@NonNull Set<Object> seasons;
	
	public SeriesMedia(int id, String status, ExternalIds externalIds, @Nullable MediaInfo mediaInfo, String name, String originalName, Set<Object> seasons){
		super(id, status, externalIds, mediaInfo);
		this.name = name;
		this.originalName = originalName;
		this.seasons = Optional.ofNullable(seasons).orElseGet(Set::of);
	}
}
