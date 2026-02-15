package fr.rakambda.plexdeleter.service.data;

public record DeleteMediaResponse(
		boolean deletedDatabase,
		boolean deletedServarr,
		boolean deletedSeerr
){
}
