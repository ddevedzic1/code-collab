package com.codecollab.execution_service.support;

import jakarta.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codecollab.execution_service.config.FlywayConfig;

/**
 * Base class for repository integration tests backed by a real PostgreSQL
 * (via Testcontainers) with the production Flyway migrations applied.
 *
 * <p>{@code @Testcontainers(disabledWithoutDocker = true)} makes every subclass
 * self-skip when Docker is not available, so a plain {@code mvn test} never fails
 * on a machine without Docker. {@link FlywayConfig} is imported so the application
 * schema is created and migrations run exactly as in production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(FlywayConfig.class)
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractRepositoryIT {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("app.database.schema", () -> "execution_service");
		registry.add("spring.flyway.default-schema", () -> "execution_service");
		registry.add("spring.jpa.properties.hibernate.default_schema", () -> "execution_service");
		registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
	}

	protected static Statistics statistics(EntityManager entityManager) {
		return entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
	}
}
