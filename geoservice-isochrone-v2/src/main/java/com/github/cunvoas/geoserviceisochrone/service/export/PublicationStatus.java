package com.github.cunvoas.geoserviceisochrone.service.export;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		/** Fichiers produits, en attente du dashboard et du commit final. */
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

	public enum StepState {
		PENDING, RUNNING, DONE, FAILED
	}

	@Data
	public static class StepStatus {
		private String code;
		private String label;
		/** Poids relatif de l'étape dans l'ETA, en pourcentage. */
		private int weight;
		private StepState state = StepState.PENDING;
		private LocalDateTime startedAt;
		private LocalDateTime endedAt;

		public StepStatus() {
		}

		public StepStatus(String code, String label, int weight) {
			this.code = code;
			this.label = label;
			this.weight = weight;
		}

		public StepStatus copy() {
			StepStatus copy = new StepStatus(code, label, weight);
			copy.setState(state);
			copy.setStartedAt(startedAt);
			copy.setEndedAt(endedAt);
			return copy;
		}
	}

	@Data
	public static class FileStatus {
		private String type;
		private String path;
		private boolean produced;
		private long size;
		private LocalDateTime date;
		private String message;

		public FileStatus copy() {
			FileStatus copy = new FileStatus();
			copy.setType(type);
			copy.setPath(path);
			copy.setProduced(produced);
			copy.setSize(size);
			copy.setDate(date);
			copy.setMessage(message);
			return copy;
		}
	}

	private Long com2coId;
	private Integer year;
	private Phase phase;
	private List<FileStatus> files = new ArrayList<>();
	private List<StepStatus> steps = new ArrayList<>();
	private Map<String, Long> objects = new LinkedHashMap<>();
	private String gitSha;
	private String message;
	private String currentStepCode;
	private String currentStepLabel;
	private int currentStepIndex;
	private int currentStepTotal;
	private int cityIndex;
	private int cityTotal;
	private String cityInsee;
	private String cityName;
	private int filesProduced;
	private int filesExpected;
	private Long etaSeconds;
	private LocalDateTime startedAt;
	private LocalDateTime updatedAt;

	public synchronized void startStep(String code) {
		for (StepStatus step : steps) {
			if (step.getCode().equals(code)) {
				step.setState(StepState.RUNNING);
				step.setStartedAt(LocalDateTime.now());
				currentStepCode = step.getCode();
				currentStepLabel = step.getLabel();
				currentStepIndex = steps.indexOf(step) + 1;
			}
		}
		currentStepTotal = steps.size();
		cityIndex = 0;
		cityTotal = 0;
		cityInsee = null;
		cityName = null;
		touch();
	}

	public synchronized void endStep(String code) {
		for (StepStatus step : steps) {
			if (step.getCode().equals(code)) {
				step.setState(StepState.DONE);
				step.setEndedAt(LocalDateTime.now());
			}
		}
		cityIndex = 0;
		cityTotal = 0;
		cityInsee = null;
		cityName = null;
		touch();
	}

	public synchronized void failStep(String code) {
		for (StepStatus step : steps) {
			if (step.getCode().equals(code)) {
				step.setState(StepState.FAILED);
				step.setEndedAt(LocalDateTime.now());
			}
		}
		touch();
	}

	public synchronized void city(int index, int total, String insee, String name) {
		cityIndex = index;
		cityTotal = total;
		cityInsee = insee;
		cityName = name;
		touch();
	}

	public synchronized void objects(String type, long count) {
		objects.merge(type, count, Long::sum);
		touch();
	}

	public synchronized void addFile(String type, String path, boolean produced, long size, LocalDateTime date, String message) {
		FileStatus fs = new FileStatus();
		fs.setType(type);
		fs.setPath(path);
		fs.setProduced(produced);
		fs.setSize(size);
		fs.setDate(date);
		fs.setMessage(message);
		files.add(fs);
		filesExpected = files.size();
		if (produced) {
			filesProduced++;
		}
		touch();
	}

	public synchronized void touch() {
		updatedAt = LocalDateTime.now();
		refreshEta();
	}

	/**
	 * ETA approximative à partir des poids d'étapes et de l'avancement de l'étape en cours.
	 * Null tant que la progression est trop faible pour être significative.
	 */
	public synchronized void refreshEta() {
		if (startedAt == null) {
			etaSeconds = null;
			return;
		}
		int totalWeight = steps.stream().mapToInt(StepStatus::getWeight).sum();
		if (totalWeight <= 0) {
			etaSeconds = null;
			return;
		}
		double progress = 0;
		for (StepStatus step : steps) {
			if (step.getState() == StepState.DONE) {
				progress += step.getWeight();
			} else if (step.getState() == StepState.RUNNING) {
				double partial = cityTotal > 0 ? Math.min(1.0, (double) cityIndex / cityTotal) : 0.0;
				progress += step.getWeight() * partial;
			}
		}
		progress = progress / totalWeight;
		if (progress < 0.05 || progress >= 1.0) {
			etaSeconds = null;
			return;
		}
		long elapsed = Math.max(0, Duration.between(startedAt, LocalDateTime.now()).toSeconds());
		etaSeconds = Math.max(0, Math.round(elapsed * (1.0 - progress) / progress));
	}

	public boolean hasProductionError() {
		boolean stepFailed = steps.stream().anyMatch(step -> step.getState() == StepState.FAILED);
		boolean fileMissing = files.stream().anyMatch(file -> !file.isProduced());
		return stepFailed || fileMissing;
	}

	/**
	 * Copie profonde destinée à la sérialisation HTTP, sans exposer l'objet mutable du service.
	 */
	public synchronized PublicationStatus copy() {
		PublicationStatus copy = new PublicationStatus();
		copy.setCom2coId(com2coId);
		copy.setYear(year);
		copy.setPhase(phase);
		copy.setGitSha(gitSha);
		copy.setMessage(message);
		copy.setCurrentStepCode(currentStepCode);
		copy.setCurrentStepLabel(currentStepLabel);
		copy.setCurrentStepIndex(currentStepIndex);
		copy.setCurrentStepTotal(currentStepTotal);
		copy.setCityIndex(cityIndex);
		copy.setCityTotal(cityTotal);
		copy.setCityInsee(cityInsee);
		copy.setCityName(cityName);
		copy.setFilesProduced(filesProduced);
		copy.setFilesExpected(filesExpected);
		copy.setEtaSeconds(etaSeconds);
		copy.setStartedAt(startedAt);
		copy.setUpdatedAt(updatedAt);
		copy.setFiles(files.stream().map(FileStatus::copy).toList());
		copy.setSteps(steps.stream().map(StepStatus::copy).toList());
		copy.setObjects(new LinkedHashMap<>(objects));
		return copy;
	}
}
