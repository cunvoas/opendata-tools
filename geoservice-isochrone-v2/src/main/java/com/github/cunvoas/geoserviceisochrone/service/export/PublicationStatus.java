package com.github.cunvoas.geoserviceisochrone.service.export;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Statut d'une publication : production des fichiers GeoJSON puis publication Git.
 * Conservé en mémoire par {@link PublishService} et exposé à l'admin via /mvc/ajax/publish/api/status.
 */
@Data
public class PublicationStatus {

	public enum Phase {
		/** Production des fichiers en cours. */
		PRODUCING,
		/** Production terminée avec au moins une erreur : aucune publication Git tentée. */
		PRODUCTION_FAILED,
		/** Fichiers produits, publication Git pas encore démarrée. */
		PRODUCED,
		/** Commit/push Git en cours. */
		PUBLISHING,
		/** Commit poussé. */
		PUBLISHED,
		/** Production OK mais push en échec. */
		PUSH_FAILED,
		/** Production OK mais aucun credential Git configuré. */
		PUSH_NOT_CONFIGURED
	}

	@Data
	public static class FileStatus {
		private String type;
		private String path;
		private boolean produced;
		private long size;
		private LocalDateTime date;
		private String message;
	}

	private Long com2coId;
	private Integer year;
	private Phase phase;
	private List<FileStatus> files = new ArrayList<>();
	private String gitSha;
	private String message;
	private LocalDateTime startedAt;
	private LocalDateTime updatedAt;

	public void addFile(String type, String path, boolean produced, long size, LocalDateTime date, String message) {
		FileStatus fs = new FileStatus();
		fs.setType(type);
		fs.setPath(path);
		fs.setProduced(produced);
		fs.setSize(size);
		fs.setDate(date);
		fs.setMessage(message);
		files.add(fs);
	}

	public boolean hasProductionError() {
		return files.stream().anyMatch(f -> !f.isProduced() && f.getMessage() != null);
	}
}
