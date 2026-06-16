package com.codecollab.snippet_service.share.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.model.SnippetShare;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.support.AbstractRepositoryIT;

class SnippetShareRepositoryIT extends AbstractRepositoryIT {

	@Autowired
	private SnippetShareRepository snippetShareRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Snippet persistSnippet() {
		var language = new Language();
		language.setCode("py");
		language.setName("Python");
		language.setVersion("3.11");
		language.setRuntimeImage("python:3.11-slim");
		language.setStartDate(LocalDateTime.now());
		entityManager.persistAndFlush(language);

		var snippet = new Snippet();
		snippet.setUserId(UUID.randomUUID());
		snippet.setLanguage(language);
		snippet.setTitle("Title");
		snippet.setContent("code");
		return entityManager.persistAndFlush(snippet);
	}

	private SnippetShare persistShare(Snippet snippet, String token) {
		var share = new SnippetShare();
		share.setSnippet(snippet);
		share.setShareToken(token);
		share.setShareType(ShareType.PUBLIC_LINK);
		share.setPermission(Permission.READ_ONLY);
		return entityManager.persistAndFlush(share);
	}

	@Test
	void findByShareToken_returnsShareWithSnippetAndLanguageFetched() {
		var snippet = persistSnippet();
		persistShare(snippet, "tok-1");
		entityManager.clear();

		var found = snippetShareRepository.findByShareToken("tok-1");

		assertThat(found).isPresent();
		assertThat(found.get().getSnippet().getLanguage().getRuntimeImage()).isEqualTo("python:3.11-slim");
	}

	@Test
	void findBySnippetId_returnsActiveShare() {
		var snippet = persistSnippet();
		persistShare(snippet, "tok-2");
		entityManager.clear();

		assertThat(snippetShareRepository.findBySnippetId(snippet.getId())).isPresent();
	}

	@Test
	void existsBySnippetId_isTrue_whenShareExists() {
		var snippet = persistSnippet();
		persistShare(snippet, "tok-3");
		entityManager.clear();

		assertThat(snippetShareRepository.existsBySnippetId(snippet.getId())).isTrue();
	}

	@Test
	void findByShareToken_excludesSoftDeletedShare() {
		var snippet = persistSnippet();
		var share = persistShare(snippet, "tok-4");
		share.setEndDate(LocalDateTime.now());
		entityManager.persistAndFlush(share);
		entityManager.clear();

		assertThat(snippetShareRepository.findByShareToken("tok-4")).isEmpty();
	}

	@Test
	void findByShareToken_fetchesSnippetAndLanguageWithoutNPlusOne() {
		var snippet = persistSnippet();
		persistShare(snippet, "tok-5");
		entityManager.clear();

		var stats = statistics(entityManager.getEntityManager());
		stats.clear();

		var found = snippetShareRepository.findByShareToken("tok-5");
		found.ifPresent(share -> share.getSnippet().getLanguage().getRuntimeImage());

		assertThat(found).isPresent();
		assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
	}
}
