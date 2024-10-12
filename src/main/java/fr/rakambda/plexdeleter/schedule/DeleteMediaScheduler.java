package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeleteMediaScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final SupervisionService supervisionService;
	private final TautulliService tautulliService;
	private final int daysDelay;
	private final boolean dryDelete;
	private final Map<String, String> remotePathMappings;
	private final OverseerrService overseerrService;
	
	@Autowired
	public DeleteMediaScheduler(MediaRepository mediaRepository, SupervisionService supervisionService, TautulliService tautulliService, ApplicationConfiguration applicationConfiguration, OverseerrService overseerrService){
		this.mediaRepository = mediaRepository;
		this.supervisionService = supervisionService;
		this.tautulliService = tautulliService;
		this.daysDelay = applicationConfiguration.getDeletion().getDaysDelay();
		this.dryDelete = applicationConfiguration.getDeletion().isDryDelete();
		this.remotePathMappings = applicationConfiguration.getDeletion().getRemotePathMappings();
		this.overseerrService = overseerrService;
	}
	
	@Override
	@NotNull
	public String getTaskId(){
		return "media-delete";
	}
	
	@Override
	@Scheduled(cron = "0 0 1 * * *")
	public void run(){
		log.info("Staring to delete old media");
		var medias = mediaRepository.findAllByStatusIn(Set.of(MediaStatus.PENDING_DELETION));
		var size = 0L;
		for(var media : medias){
			try{
				size += delete(media);
			}
			catch(Exception e){
				log.error("Failed to delete media {}", media, e);
			}
		}
		
		log.info("Deleted {} media for a total size of {}", medias.size(), size);
		supervisionService.send("♻\uFE0F✅ Deleted a total of %s", supervisionService.sizeToHuman(size));
		
		if(size > 0){
			refreshOverseerr();
		}
	}
	
	private boolean refreshOverseerr(){
		try{
			log.info("Starting Overseerr refresh");
			return overseerrService.plexSync(false, true).isRunning();
		}
		catch(RequestFailedException e){
			log.error("Failed to refresh Overseerr after deletion");
			return false;
		}
	}
	
	@VisibleForTesting
	long delete(MediaEntity mediaEntity) throws DeletionException, RequestFailedException, IOException{
		log.info("Deleting media {}", mediaEntity);
		if(Objects.isNull(mediaEntity.getPlexId())){
			throw new DeletionException("Cannot delete media %s as it does not have any Plex Id".formatted(mediaEntity));
		}
		
		var ratingKeys = tautulliService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType());
		var metadata = new LinkedHashSet<GetMetadataResponse>();
		for(var ratingKey : ratingKeys){
			var metadataResponse = tautulliService.getMetadata(ratingKey).getResponse().getData();
			if(Objects.nonNull(metadataResponse)){
				metadata.add(metadataResponse);
			}
		}
		
		if(metadata.isEmpty()){
			throw new DeletionException("Could not find metadata & files for %s".formatted(mediaEntity));
		}
		
		if(metadata.stream()
				.map(GetMetadataResponse::getAddedAt)
				.max(Comparator.naturalOrder())
				.map(date -> date.isAfter(ZonedDateTime.now().minusDays(daysDelay).toInstant()))
				.orElse(false)){
			log.info("Skipped because most recent file is not older than {} days", daysDelay);
			return 0;
		}
		
		var files = metadata.stream()
				.map(GetMetadataResponse::getMediaInfo)
				.flatMap(java.util.Collection::stream)
				.map(MediaInfo::getParts)
				.flatMap(java.util.Collection::stream)
				.map(MediaPart::getFile)
				.map(this::extractPath)
				.distinct()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		var paths = new PriorityQueue<>(Map.Entry.<Path, Path> comparingByKey().reversed());
		paths.addAll(files.entrySet());
		
		var size = 0L;
		try{
			size = deleteRecursive(paths);
		}
		catch(IOException e){
			supervisionService.send("♻\uFE0F❌ Failed to delete media %s in %s because of %s", mediaEntity, paths, e.getMessage());
			throw e;
		}
		
		if(!dryDelete){
			mediaEntity.setStatus(MediaStatus.DELETED);
			mediaEntity.setPlexId(null);
			mediaEntity.setRootPlexId(null);
			mediaEntity = mediaRepository.save(mediaEntity);
			deleteFromOverseerr(mediaEntity);
		}
		
		supervisionService.send("♻\uFE0F Deleted media %s with a size of %s", mediaEntity, supervisionService.sizeToHuman(size));
		
		return size;
	}
	
	@NotNull
	private Map.Entry<Path, Path> extractPath(@NotNull String file){
		for(var mapping : remotePathMappings.entrySet()){
			if(file.startsWith(mapping.getKey())){
				try{
					var rootMapping = Paths.get(mapping.getValue());
					var relativePath = Paths.get(file.substring(mapping.getKey().length()));
					if(Objects.nonNull(relativePath.getRoot())){
						relativePath = relativePath.getRoot().relativize(relativePath);
					}
					return Map.entry(rootMapping, rootMapping.resolve(relativePath));
				}
				catch(InvalidPathException e){
					supervisionService.send("♻\uFE0F❌ Failed to map file %s because of %s", mapping, e.getMessage());
					throw e;
				}
			}
		}
		throw new IllegalStateException("Could not find path mapping for " + file);
	}
	
	private long deleteRecursive(@NotNull Queue<Map.Entry<Path, Path>> paths) throws IOException{
		var sizeDeleted = new AtomicLong(0);
		while(!paths.isEmpty()){
			var pathEntry = paths.poll();
			var rootPath = pathEntry.getValue();
			var path = pathEntry.getKey();
			
			if(Objects.equals(rootPath, path)){
				continue;
			}
			
			if(Files.isDirectory(path)){
				deleteDirectory(path, sizeDeleted).stream()
						.map(p -> Map.entry(p, rootPath))
						.forEach(paths::add);
			}
			else if(Files.isRegularFile(path)){
				deleteFile(path, sizeDeleted);
				paths.add(Map.entry(path.getParent(), rootPath));
			}
		}
		return sizeDeleted.get();
	}
	
	@NotNull
	private Collection<Path> deleteDirectory(@NotNull Path path, @NotNull AtomicLong sizeDeleted) throws IOException{
		try(var list = Files.list(path)){
			var children = list.toList();
			for(var child : children){
				deleteCompanion(child, sizeDeleted);
			}
		}
		
		try(var list = Files.list(path)){
			var children = list.toList();
			if(!children.isEmpty()){
				return List.of();
			}
			
			log.info("Deleting folder {}", path);
			if(!dryDelete){
				Files.delete(path);
			}
			
			supervisionService.send("\uD83D\uDEAE Deleted folder %s", path);
			return List.of(path.getParent());
		}
	}
	
	private void deleteCompanion(@NotNull Path path, @NotNull AtomicLong sizeDeleted) throws IOException{
		var filename = path.getFileName().toString();
		try{
			if(Files.isRegularFile(path)){
				if(filename.matches(".*\\.(srt|smi|xml|nfo|metathumb|png|jpg|jpeg)")){
					deleteFile(path, sizeDeleted);
				}
				return;
			}
		}
		catch(IOException e){
			throw new IOException("Failed to delete companion file " + path.toAbsolutePath(), e);
		}
		
		try{
			if(Files.isDirectory(path)){
				if(Objects.equals(filename, "@eaDir") || Objects.equals(filename, "Plex Versions")){
					Files.walkFileTree(path, new FileVisitor<>(){
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
							return FileVisitResult.CONTINUE;
						}
						
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
							deleteFile(path, sizeDeleted);
							return FileVisitResult.CONTINUE;
						}
						
						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc){
							log.error("Failed to delete file in companion directory {}", file.toAbsolutePath(), exc);
							return FileVisitResult.TERMINATE;
						}
						
						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException{
							if(Objects.nonNull(exc)){
								return FileVisitResult.TERMINATE;
							}
							deleteDirectory(path, sizeDeleted);
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
		}
		catch(IOException e){
			log.warn("Failed to delete companion folder {}", path.toAbsolutePath(), e);
		}
	}
	
	private void deleteFile(@NotNull Path path, @NotNull AtomicLong sizeDeleted) throws IOException{
		var size = Files.size(path);
		if(!dryDelete){
			Files.delete(path);
		}
		sizeDeleted.addAndGet(size);
		supervisionService.send("\uD83D\uDEAE Deleted file %s for a size of %s", path, supervisionService.sizeToHuman(size));
	}
	
	private void deleteFromOverseerr(@NotNull MediaEntity mediaEntity){
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			return;
		}
		try{
			var data = overseerrService.getMediaDetails(mediaEntity.getOverseerrId(), mediaEntity.getType().getOverseerrType());
			var internalId = Optional.ofNullable(data.getMediaInfo())
					.map(fr.rakambda.plexdeleter.api.overseerr.data.MediaInfo::getId)
					.orElseThrow(() -> new IllegalStateException("Couldn't find internal media id on Overseerr"));
			overseerrService.deleteMedia(internalId);
		}
		catch(Exception e){
			log.error("Failed to delete media {} on Overseerr", mediaEntity);
		}
	}
	
	@VisibleForTesting
	static class DeletionException extends Exception{
		public DeletionException(@NotNull String message){
			super(message);
		}
	}
}
