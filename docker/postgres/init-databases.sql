-- Creates one database per microservice on first container start.
-- Each service's FlywayConfig creates its own schema and runs migrations,
-- so this only needs to provision the empty databases.
CREATE DATABASE user_service_db;
CREATE DATABASE snippet_service_db;
CREATE DATABASE execution_service_db;
CREATE DATABASE system_events_service_db;
