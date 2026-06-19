package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TestGitHelper {
	
	private GitHelper tested;

	@BeforeEach
	void setUp() {
		tested = new GitHelper();
	}

	@Test
	void testAddCommitPushSuccess(@TempDir Path tempDir) throws Exception {
		// Arrange - Create actual .git folder (required for validation)
		File gitDir = new File(tempDir.toFile(), ".git");
		gitDir.mkdir();
		
		// Create data folder
		File dataFolder = new File(tempDir.toFile(), "data");
		dataFolder.mkdir();
		
		// Create a test file in data folder
		File testFile = new File(dataFolder, "test.json");
		testFile.createNewFile();

		Git gitMock = mock(Git.class);
		Repository repositoryMock = mock(Repository.class);
		AddCommand addCommandMock = mock(AddCommand.class);
		CommitCommand commitCommandMock = mock(CommitCommand.class);
		PushCommand pushCommandMock = mock(PushCommand.class);

		when(gitMock.getRepository()).thenReturn(repositoryMock);
		when(repositoryMock.getWorkTree()).thenReturn(tempDir.toFile());
		when(gitMock.add()).thenReturn(addCommandMock);
		when(addCommandMock.call()).thenReturn(null);
		when(gitMock.commit()).thenReturn(commitCommandMock);
		when(commitCommandMock.setMessage(anyString())).thenReturn(commitCommandMock);
		when(commitCommandMock.setAuthor(anyString(), anyString())).thenReturn(commitCommandMock);
		when(commitCommandMock.call()).thenReturn(null);
		when(gitMock.push()).thenReturn(pushCommandMock);
		when(pushCommandMock.setRemote(anyString())).thenReturn(pushCommandMock);
		when(pushCommandMock.setCredentialsProvider(any(CredentialsProvider.class))).thenReturn(pushCommandMock);
		when(pushCommandMock.call()).thenReturn(Collections.emptyList());

		// Set credentials
		ReflectionTestUtils.setField(tested, "username", "testuser");
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		// Mock Git.open to return our mock
		try (MockedStatic<Git> gitStatic = mockStatic(Git.class)) {
			gitStatic.when(() -> Git.open(any(File.class))).thenReturn(gitMock);

			// Act
			tested.addCommitPush(tempDir.toFile().getAbsolutePath(), "data");

			// Assert
			verify(gitMock).add();
			verify(addCommandMock).call();
			verify(gitMock).commit();
			verify(commitCommandMock).setMessage(matches("chore\\(data\\): automatic data update \\d{4}-\\d{2}-\\d{2}T.*"));
			verify(commitCommandMock).setAuthor("testuser", "testuser@local");
			verify(gitMock).push();
			verify(pushCommandMock).setRemote("origin");
			verify(pushCommandMock).setCredentialsProvider(any(CredentialsProvider.class));
		}
	}

	@Test
	void testAddCommitPushWithCredentialsMissing_NoUsername() {
		// Arrange
		ReflectionTestUtils.setField(tested, "username", null);
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			tested.addCommitPush("/fake/repo", "data");
		});
		assertEquals("Git credentials missing (GIT_USERNAME / GIT_TOKEN)", exception.getMessage());
	}

	@Test
	void testAddCommitPushWithCredentialsMissing_NoToken() {
		// Arrange
		ReflectionTestUtils.setField(tested, "username", "testuser");
		ReflectionTestUtils.setField(tested, "token", "");

		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			tested.addCommitPush("/fake/repo", "data");
		});
		assertEquals("Git credentials missing (GIT_USERNAME / GIT_TOKEN)", exception.getMessage());
	}

	@Test
	void testAddCommitPushWithInvalidRepository_DirectoryNotExists() {
		// Arrange
		ReflectionTestUtils.setField(tested, "username", "testuser");
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			tested.addCommitPush("/nonexistent/repo", "data");
		});
		assertTrue(exception.getMessage().contains("Repository root invalid or .git missing"));
	}

	@Test
	void testAddCommitPushWithInvalidRepository_NoGitFolder(@TempDir Path tempDir) {
		// Arrange
		ReflectionTestUtils.setField(tested, "username", "testuser");
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			tested.addCommitPush(tempDir.toFile().getAbsolutePath(), "data");
		});
		assertTrue(exception.getMessage().contains("Repository root invalid or .git missing"));
	}

	@Test
	void testAddCommitPushWithNonExistentFolder(@TempDir Path tempDir) throws Exception {
		// Arrange
		File gitDir = new File(tempDir.toFile(), ".git");
		gitDir.mkdir();

		Git gitMock = mock(Git.class);
		Repository repositoryMock = mock(Repository.class);

		when(gitMock.getRepository()).thenReturn(repositoryMock);
		when(repositoryMock.getWorkTree()).thenReturn(tempDir.toFile());

		ReflectionTestUtils.setField(tested, "username", "testuser");
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		try (MockedStatic<Git> gitStatic = mockStatic(Git.class)) {
			gitStatic.when(() -> Git.open(any(File.class))).thenReturn(gitMock);

			// Act & Assert
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
				tested.addCommitPush(tempDir.toFile().getAbsolutePath(), "nonexistent");
			});
			assertTrue(exception.getMessage().contains("Folder does not exist"));
		}
	}

	@Test
	void testAddCommitPushWithWhitespaceCredentials() {
		// Arrange
		ReflectionTestUtils.setField(tested, "username", "   ");
		ReflectionTestUtils.setField(tested, "token", "testtoken");

		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			tested.addCommitPush("/fake/repo", "data");
		});
		assertEquals("Git credentials missing (GIT_USERNAME / GIT_TOKEN)", exception.getMessage());
	}

}
