package com.codecollab.snippet_service.share.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codecollab.snippet_service.share.model.SnippetShareUser;

public interface SnippetShareUserRepository extends JpaRepository<SnippetShareUser, UUID> {

	List<SnippetShareUser> findBySnippetShareId(UUID snippetShareId);

	Optional<SnippetShareUser> findBySnippetShareIdAndUserId(UUID snippetShareId, UUID userId);

	boolean existsBySnippetShareIdAndUserId(UUID snippetShareId, UUID userId);
}
