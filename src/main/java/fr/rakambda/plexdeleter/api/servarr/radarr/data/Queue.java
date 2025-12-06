package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Queue.class)
public record Queue(int id){
}
