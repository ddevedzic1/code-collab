package com.codecollab.snippet_service.share.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codecollab.snippet_service.share.model.SnippetShare;

public interface SnippetShareRepository extends JpaRepository<SnippetShare, UUID> {

	Optional<SnippetShare> findBySnippetId(UUID snippetId);

	@Query("""
			SELECT s FROM SnippetShare s
			JOIN FETCH s.snippet sn
			JOIN FETCH sn.language
			WHERE s.shareToken = :shareToken
			""")
	Optional<SnippetShare> findByShareToken(@Param("shareToken") String shareToken);

	boolean existsBySnippetId(UUID snippetId);
}
