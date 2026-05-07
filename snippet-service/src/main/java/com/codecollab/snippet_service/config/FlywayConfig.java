package com.codecollab.snippet_service.config;

import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codecollab.snippet_service.exception.AppException;

@Configuration
public class FlywayConfig {

	private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

	private final String schema;

	public FlywayConfig(@Value("${app.database.schema}") String schema) {
		this.schema = schema;
	}

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy(DataSource dataSource) {
		return flyway -> {
			validateSchemaName(schema);
			try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
				var schemaCreationSql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schema);
				statement.execute(schemaCreationSql);
			} catch (Exception e) {
				throw new AppException(AppException.DATABASE_ERROR,
						"Failed to create database schema " + schema, e);
			}

			flyway.migrate();
		};
	}

	private void validateSchemaName(String name) {
		if (name == null || name.isBlank() || !SAFE_IDENTIFIER.matcher(name).matches()) {
			throw new AppException(AppException.DATABASE_ERROR,
					"Invalid database schema name: " + name);
		}
	}
}
