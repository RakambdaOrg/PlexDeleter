package fr.rakambda.plexdeleter.service;

public record DeleteMediaResponse(
		boolean deletedDatabase,
		boolean deletedServarr,
		boolean deletedOverseerr
){
}
