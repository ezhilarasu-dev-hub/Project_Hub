package com.projecthub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initializes required filesystem directories before DataSource bean creation,
 * ensuring SQLite never fails on startup due to a missing parent directory
 * (e.g. /var/lib/project-hub on Ubuntu EC2).
 */
public class DirectoryInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // 1. Ensure SQLite database parent directory exists
        String dbPath = env.getProperty("PROJECT_HUB_DB_PATH");
        if (dbPath == null || dbPath.isBlank()) {
            String datasourceUrl = env.getProperty("spring.datasource.url");
            if (datasourceUrl != null && datasourceUrl.startsWith("jdbc:sqlite:")) {
                dbPath = datasourceUrl.substring("jdbc:sqlite:".length()).trim();
            }
        }

        if (dbPath != null && !dbPath.isBlank()) {
            try {
                Path databasePath = Paths.get(dbPath).toAbsolutePath().normalize();
                Path parentDir = databasePath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                    logger.info("Initialized missing database directory: {}", parentDir);
                }
            } catch (Exception ex) {
                logger.warn("Unable to pre-create database directory for path {}: {}", dbPath, ex.getMessage());
            }
        }

        // 2. Ensure base upload directory exists
        String uploadDir = env.getProperty("PROJECT_HUB_UPLOAD_DIR",
                env.getProperty("app.storage.upload-dir", "./uploads/projects/"));
        if (uploadDir != null && !uploadDir.isBlank()) {
            try {
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    logger.info("Initialized missing upload directory: {}", uploadPath);
                }
            } catch (Exception ex) {
                logger.warn("Unable to pre-create upload directory for path {}: {}", uploadDir, ex.getMessage());
            }
        }
    }
}
