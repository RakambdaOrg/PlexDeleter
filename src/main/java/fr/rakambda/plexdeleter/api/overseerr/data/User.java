package fr.rakambda.plexdeleter.api.overseerr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(User.class)
public record User(int plexId){
}
