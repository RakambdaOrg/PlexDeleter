package fr.rakambda.plexdeleter.service.data;

import fr.rakambda.plexdeleter.storage.entity.MediaType;

public record LibraryElement(
		int ratingKey,
		String name,
		MediaType type
){
}
