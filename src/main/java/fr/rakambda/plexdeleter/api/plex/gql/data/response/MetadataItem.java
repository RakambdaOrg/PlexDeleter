package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(MetadataItem.class)
public record MetadataItem(int index){
}
