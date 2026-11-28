package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Classe utilitaire pour l'intégration de fonctionnalités Git.
 * (À compléter selon les besoins du projet)
 * @author cunvoas
 * @see https://git-scm.com/book/en/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
 */
@Component
@Slf4j
public class GitHelper {
	

	/*
	 * Client Git léger basé sur JGit pour réaliser :
	 *  - git add "dossier/."
	 *  - git commit -m "chore: data for YYYY-MM-DD hh:mm:ss"
	 *  - git push (origin) avec login/token fournis dans application-prod.yml
	 *
	 * Configuration attendue (application-prod.yml) :
	 * application:
	 *   git:
	 *     localPath: ${MOUNT_PATH}/isochrone/data
	 *     username: ${GIT_USERNAME}
	 *     token: ${GIT_TOKEN}
	 */

    @Value("${application.git.username:}")
	private String username;
    @Value("${application.git.token:}")
	private String token;
    @Value("${application.git.localPath:}")
	private String localPath;
    @Value("${application.feature-flipping.publishGeoData:false}")
	private boolean publishGeoData;

	/**
	 * @return chemin du clone local du dépôt de publication (peut être vide)
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @return true si les credentials Git sont renseignés
	 */
	public boolean isConfigured() {
		return !isBlank(username) && !isBlank(token);
	}

	/**
	 * @return true si les credentials Git sont renseignés
	 */
	public boolean isAutorisedToPublish() {
		return publishGeoData;
	}


	/**
	 * Ajoute tous les fichiers des dossiers (pattern JGit), crée un commit avec la date/heure du jour et pousse sur origin.
	 * @param repositoryRoot chemin absolu vers la racine du repository (où se trouve .git)
	 * @param folderPaths chemins relatifs (depuis la racine du repo) des dossiers à ajouter
	 * @return le SHA du commit créé, ou null si le commit n'a pas retourné d'objet
	 * @throws IllegalStateException si credentials absents
	 */
	public String addCommitPush(String repositoryRoot, String... folderPaths) throws Exception {
		if (!isConfigured()) {
			throw new IllegalStateException("Git credentials missing (GIT_USERNAME / GIT_TOKEN)");
		}
		if (folderPaths == null || folderPaths.length == 0) {
			throw new IllegalArgumentException("No folder to publish");
		}

		java.io.File repoDir = new java.io.File(repositoryRoot);
		if (!repoDir.exists() || !new java.io.File(repoDir, ".git").exists()) {
			throw new IllegalArgumentException("Repository root invalid or .git missing: " + repositoryRoot);
		}

		if (publishGeoData) {
			try (Git git = Git.open(repoDir)) {
				for (String folderPath : folderPaths) {
					stageFolder(git, folderPath);
				}
				String sha = commit(git, folderPaths[0]);
				push(git);
				return sha;
			}
		} else {
			log.info("git publication is disabled");
			return null;
		}
	}

	/**
	 * État local du dépôt : HEAD et branche courante.
	 * @param repositoryRoot racine du dépôt
	 * @return HEAD court et branche
	 * @throws Exception si le dépôt est illisible
	 */
	public RepoState state(String repositoryRoot) throws Exception {
		java.io.File repoDir = new java.io.File(repositoryRoot);
		if (!repoDir.exists() || !new java.io.File(repoDir, ".git").exists()) {
			return new RepoState(null, null, false);
		}
		try (Git git = Git.open(repoDir)) {
			ObjectId head = git.getRepository().resolve(Constants.HEAD);
			String branch = git.getRepository().getBranch();
			return new RepoState(head == null ? null : head.getName(), branch, true);
		}
	}

	/**
	 * Liste les chemins relatifs modifiés, nouveaux ou non suivis.
	 * @param repositoryRoot racine du dépôt
	 * @return chemins relatifs triés
	 * @throws Exception si le dépôt est illisible
	 */
	public List<String> changedPaths(String repositoryRoot) throws Exception {
		java.io.File repoDir = new java.io.File(repositoryRoot);
		if (!repoDir.exists() || !new java.io.File(repoDir, ".git").exists()) {
			return List.of();
		}
		try (Git git = Git.open(repoDir)) {
			Status status = git.status().call();
			TreeSet<String> paths = new TreeSet<>();
			paths.addAll(status.getAdded());
			paths.addAll(status.getChanged());
			paths.addAll(status.getModified());
			paths.addAll(status.getUntracked());
			paths.addAll(status.getRemoved());
			paths.addAll(status.getMissing());
			return new ArrayList<>(paths);
		}
	}

	/**
	 * Commit final d'une liste exacte de fichiers, puis push si la publication est configurée et autorisée.
	 * Aucun `git add -A` : seuls les fichiers demandés sont stage.
	 * @param repositoryRoot racine du dépôt
	 * @param relativeFiles chemins relatifs à la racine
	 * @param message message de commit
	 * @return SHA, état du push et message éventuel
	 * @throws Exception si le commit échoue
	 */
	public CommitResult commitFiles(String repositoryRoot, List<String> relativeFiles, String message) throws Exception {
		if (relativeFiles == null || relativeFiles.isEmpty()) {
			throw new IllegalArgumentException("No file to commit");
		}
		java.io.File repoDir = new java.io.File(repositoryRoot);
		if (!repoDir.exists() || !new java.io.File(repoDir, ".git").exists()) {
			throw new IllegalArgumentException("Repository root invalid or .git missing: " + repositoryRoot);
		}
		try (Git git = Git.open(repoDir)) {
			Status before = git.status().call();
			java.util.Set<String> staged = new java.util.HashSet<>();
			staged.addAll(before.getAdded());
			staged.addAll(before.getChanged());
			for (String path : staged) {
				if (!relativeFiles.contains(path)) {
					throw new IllegalStateException("Index Git contient un fichier hors périmètre : " + path);
				}
			}
			AddCommand add = git.add();
			for (String file : relativeFiles) {
				if (Files.isRegularFile(Paths.get(repositoryRoot, file))) {
					add.addFilepattern(file);
				} else {
					git.rm().addFilepattern(file).call();
				}
			}
			add.call();
			String author = isBlank(username) ? "geoservice" : username;
			RevCommit rev = git.commit()
					.setMessage(message)
					.setAuthor(author, author + "@local")
					.call();
			String sha = rev == null ? null : rev.getName();
			if (isConfigured() && isAutorisedToPublish()) {
				try {
					push(git);
					return new CommitResult(sha, true, "Commit et push OK");
				} catch (Exception e) {
					log.error("Git push failed after final commit", e);
					return new CommitResult(sha, false, "Commit local OK, push en échec : " + e.getMessage());
				}
			}
			return new CommitResult(sha, false, "Commit local OK, push non configuré ou désactivé");
		}
	}

	public record RepoState(String head, String branch, boolean repositoryFound) {
	}

	public record CommitResult(String sha, boolean pushed, String message) {
	}

	private void stageFolder(Git git, String folderPath) throws Exception {
		// Ajout récursif : on parcourt les fichiers si pattern unique insuffisant
		Path base = Paths.get(git.getRepository().getWorkTree().getAbsolutePath(), folderPath);
		if (!Files.exists(base)) {
			throw new IllegalArgumentException("Folder does not exist: " + folderPath);
		}

		AddCommand add = git.add();
		// Ajouter chaque fichier relatif au repo
		try (Stream<Path> stream = Files.walk(base)) {
			stream.filter(Files::isRegularFile).forEach(p -> {
				String rel = git.getRepository().getWorkTree().toPath().relativize(p).toString().replace('\\', '/');
				add.addFilepattern(rel);
			});
		}
		add.call();
	}

	private String commit(Git git, String folderPath) throws Exception {
		
		if (publishGeoData) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
			String message = String.format("chore(%s): automatic data update %s", folderPath, timestamp);
			org.eclipse.jgit.revwalk.RevCommit rev = git.commit()
					.setMessage(message)
					.setAuthor(username, username + "@local")
					.call();
			return rev == null ? null : rev.getName();
		}
		return null;
	}

	private void push(Git git) throws Exception {
		CredentialsProvider cp =
				new UsernamePasswordCredentialsProvider(username, token);
		git.push()
				.setRemote("origin")
				.setCredentialsProvider(cp)
				.call();
	}

	private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}