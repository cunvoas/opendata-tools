package com.github.cunvoas.geoserviceisochrone.extern.helper;

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
	 *  - git commit -m "chore: data YYYY-MM-DD hh:mm:ss"
	 *  - git push (origin) avec login/token fournis dans application-prod.yml
	 *
	 * Configuration attendue (application-prod.yml) :
	 * git:
	 *   username: ${GIT_USERNAME}
	 *   token: ${GIT_TOKEN}
	 * Les variables d'environnement GIT_USERNAME / GIT_TOKEN doivent être définies.
	 */

    @Value("application.git.GIT_USERNAME")
	private String username;
    @Value("application.git.GIT_TOKEN")
	private String token;


	/**
	 * Ajoute tous les fichiers du dossier (pattern JGit), crée un commit avec la date/heure du jour et pousse sur origin.
	 * @param repositoryRoot chemin absolu vers la racine du repository (où se trouve .git)
	 * @param folderPath chemin relatif (depuis la racine du repo) vers le dossier à ajouter
	 * @throws IllegalStateException si credentials absents
	 */
	public void addCommitPush(String repositoryRoot, String folderPath) throws Exception {
		if (isBlank(username) || isBlank(token)) {
			throw new IllegalStateException("Git credentials missing (GIT_USERNAME / GIT_TOKEN)");
		}

		java.io.File repoDir = new java.io.File(repositoryRoot);
		if (!repoDir.exists() || !new java.io.File(repoDir, ".git").exists()) {
			throw new IllegalArgumentException("Repository root invalid or .git missing: " + repositoryRoot);
		}

		try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(repoDir)) {
			stageFolder(git, folderPath);
			commit(git);
			push(git);
		}
	}

	private void stageFolder(org.eclipse.jgit.api.Git git, String folderPath) throws Exception {
		// Ajout récursif : on parcourt les fichiers si pattern unique insuffisant
		java.nio.file.Path base = java.nio.file.Paths.get(git.getRepository().getWorkTree().getAbsolutePath(), folderPath);
		if (!java.nio.file.Files.exists(base)) {
			throw new IllegalArgumentException("Folder does not exist: " + folderPath);
		}

		org.eclipse.jgit.api.AddCommand add = git.add();
		// Ajouter chaque fichier relatif au repo
		try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(base)) {
			stream.filter(java.nio.file.Files::isRegularFile).forEach(p -> {
				String rel = git.getRepository().getWorkTree().toPath().relativize(p).toString().replace('\\', '/');
				add.addFilepattern(rel);
			});
		}
		add.call();
	}

	private void commit(org.eclipse.jgit.api.Git git) throws Exception {
		String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME);
		git.commit()
				.setMessage("chore: data " + date)
				.setAuthor(username, username + "@local")
				.call();
	}

	private void push(org.eclipse.jgit.api.Git git) throws Exception {
		org.eclipse.jgit.transport.CredentialsProvider cp =
				new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(username, token);
		git.push()
				.setRemote("origin")
				.setCredentialsProvider(cp)
				.call();
	}

	private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}