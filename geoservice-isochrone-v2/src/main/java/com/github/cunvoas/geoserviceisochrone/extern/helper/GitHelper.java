package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour l'intégration de fonctionnalités Git.
 * (À compléter selon les besoins du projet)
 * @author cunvoas
 * @see https://git-scm.com/book/en/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
 */
@Component
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

		try (Git git = Git.open(repoDir)) {
			for (String folderPath : folderPaths) {
				stageFolder(git, folderPath);
			}
			String sha = commit(git, folderPaths[0]);
			push(git);
			return sha;
		}
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
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
		String message = String.format("chore(%s): automatic data update %s", folderPath, timestamp);
		org.eclipse.jgit.revwalk.RevCommit rev = git.commit()
				.setMessage(message)
				.setAuthor(username, username + "@local")
				.call();
		return rev == null ? null : rev.getName();
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