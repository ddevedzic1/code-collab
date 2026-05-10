package com.codecollab.snippet_service.language.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codecollab.snippet_service.language.model.Language;

public interface LanguageRepository extends JpaRepository<Language, UUID> {

	boolean existsByCode(String code);
}
