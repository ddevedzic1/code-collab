package com.codecollab.snippet_service.snippet.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.support.AbstractRepositoryIT;

import org.springframework.data.domain.Page;

class SnippetRepositoryIT extends AbstractRepositoryIT {

	@Autowired
	private SnippetRepository snippetRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Language persistLanguage(String code) {
		var language = new Language();
		language.setCode(code);
		language.setName("Name " + code);
		language.setVersion("1.0");
		language.setRuntimeImage(code + ":latest");
		language.setStartDate(LocalDateTime.now());
		return entityManager.persistAndFlush(language);
	}

	private Snippet persistSnippet(UUID userId, Language language, String title) {
		var snippet = new Snippet();
		snippet.setUserId(userId);
		snippet.setLanguage(language);
		snippet.setTitle(title);
		snippet.setContent("code");
		return entityManager.persistAndFlush(snippet);
	}

	@Test
	void findActiveById_returnsSnippetWithLanguageFetched() {
		var language = persistLanguage("py");
		var snippet = persistSnippet(UUID.randomUUID(), language, "Title");
		entityManager.clear();

		var found = snippetRepository.findActiveById(snippet.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getLanguage().getRuntimeImage()).isEqualTo("py:latest");
	}

	@Test
	void findActiveById_excludesSoftDeletedSnippet() {
		var language = persistLanguage("js");
		var snippet = persistSnippet(UUID.randomUUID(), language, "Title");
		snippet.setEndDate(LocalDateTime.now());
		entityManager.persistAndFlush(snippet);
		entityManager.clear();

		assertThat(snippetRepository.findActiveById(snippet.getId())).isEmpty();
	}

	@Test
	void search_returnsOnlyOwnSnippets() {
		var language = persistLanguage("go");
		var owner = UUID.randomUUID();
		persistSnippet(owner, language, "Mine");
		persistSnippet(UUID.randomUUID(), language, "Theirs");
		entityManager.clear();

		var page = snippetRepository.search(owner, null, null, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().get(0).getTitle()).isEqualTo("Mine");
	}

	@Test
	void search_filtersByTitlePattern_caseInsensitively() {
		var language = persistLanguage("rb");
		var owner = UUID.randomUUID();
		persistSnippet(owner, language, "Hello World");
		persistSnippet(owner, language, "Goodbye");
		entityManager.clear();

		var page = snippetRepository.search(owner, "%hello%", null, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().get(0).getTitle()).isEqualTo("Hello World");
	}

	@Test
	void search_filtersByLanguageId() {
		var owner = UUID.randomUUID();
		var python = persistLanguage("python");
		var node = persistLanguage("node");
		persistSnippet(owner, python, "Py");
		persistSnippet(owner, node, "Js");
		entityManager.clear();

		var page = snippetRepository.search(owner, null, python.getId(), PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().get(0).getTitle()).isEqualTo("Py");
	}

	@Test
	void search_fetchesLanguageWithoutNPlusOne() {
		var owner = UUID.randomUUID();
		persistSnippet(owner, persistLanguage("py"), "A");
		persistSnippet(owner, persistLanguage("js"), "B");
		persistSnippet(owner, persistLanguage("go"), "C");
		entityManager.clear();

		var stats = statistics(entityManager.getEntityManager());
		stats.clear();

		Page<Snippet> page = snippetRepository.search(owner, null, null, PageRequest.of(0, 10));
		page.getContent().forEach(snippet -> snippet.getLanguage().getRuntimeImage());

		assertThat(page.getTotalElements()).isEqualTo(3);
		assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	void findActiveById_fetchesLanguageInSingleQuery() {
		var snippet = persistSnippet(UUID.randomUUID(), persistLanguage("rb"), "Title");
		entityManager.clear();

		var stats = statistics(entityManager.getEntityManager());
		stats.clear();

		var found = snippetRepository.findActiveById(snippet.getId());
		found.ifPresent(s -> s.getLanguage().getRuntimeImage());

		assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
	}
}
