package com.codecollab.snippet_service.snippet.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codecollab.snippet_service.snippet.model.Snippet;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {

	@Query("SELECT s FROM Snippet s JOIN FETCH s.language WHERE s.id = :id")
	Optional<Snippet> findWithLanguageById(@Param("id") UUID id);

	@Query(value = """
			SELECT s FROM Snippet s
			JOIN FETCH s.language
			WHERE s.userId = :userId
			  AND (:titlePattern IS NULL OR LOWER(s.title) LIKE :titlePattern)
			  AND (:languageId IS NULL OR s.language.id = :languageId)
			""",
			countQuery = """
					SELECT COUNT(s) FROM Snippet s
					WHERE s.userId = :userId
					  AND (:titlePattern IS NULL OR LOWER(s.title) LIKE :titlePattern)
					  AND (:languageId IS NULL OR s.language.id = :languageId)
					""")
	Page<Snippet> search(
			@Param("userId") UUID userId,
			@Param("titlePattern") String titlePattern,
			@Param("languageId") UUID languageId,
			Pageable pageable);
}
