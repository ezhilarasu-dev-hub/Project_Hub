package com.projecthub.storage;

import com.projecthub.exception.BadRequestException;
import com.projecthub.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path baseStorageLocation;

    public LocalStorageService(@Value("${app.storage.upload-dir:./uploads/projects/}") String uploadDir) {
        this.baseStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the upload directory: " + uploadDir, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        String rawName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "source_code.zip";
        // Strip any directory path components from filename to prevent path traversal
        String originalFilename = Paths.get(StringUtils.cleanPath(rawName)).getFileName().toString();

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Validate allowed file types (ZIP, TAR, GZ, RAR, 7Z)
        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".zip") && !lowerName.endsWith(".tar") && !lowerName.endsWith(".tar.gz") && !lowerName.endsWith(".gz") && !lowerName.endsWith(".7z")) {
            throw new BadRequestException("Invalid file type. Only archive files (.zip, .tar.gz, .7z) are permitted for project source code.");
        }

        try {
            Path targetDir = this.baseStorageLocation;
            if (StringUtils.hasText(subDirectory)) {
                // Sanitize subDirectory to prevent traversal
                String cleanSubDir = Paths.get(StringUtils.cleanPath(subDirectory)).getFileName().toString();
                targetDir = this.baseStorageLocation.resolve(cleanSubDir).normalize();
                if (!targetDir.startsWith(this.baseStorageLocation)) {
                    throw new BadRequestException("Invalid subdirectory target.");
                }
                Files.createDirectories(targetDir);
            }

            // Generate unique filename preserving extension
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path targetLocation = targetDir.resolve(uniqueFilename).normalize();

            if (!targetLocation.startsWith(this.baseStorageLocation)) {
                throw new BadRequestException("Cannot store file outside current storage directory.");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new ResourceNotFoundException("File path is empty or null.");
        }

        Path resolvedPath = resolveStoredPath(filePath);
        if (resolvedPath == null || !Files.exists(resolvedPath) || !Files.isReadable(resolvedPath)) {
            throw new ResourceNotFoundException("File not found or not readable: " + filePath);
        }

        try {
            return new UrlResource(resolvedPath.toUri());
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Invalid file path format: " + filePath);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        try {
            Path resolvedPath = resolveStoredPath(filePath);
            if (resolvedPath != null && resolvedPath.startsWith(this.baseStorageLocation)) {
                Files.deleteIfExists(resolvedPath);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete file {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Resolves a stored file path safely and robustly across platforms (Windows & Linux).
     * Handles relative paths, OS-specific path changes, or migrations.
     */
    private Path resolveStoredPath(String filePath) {
        // Direct absolute or relative check
        try {
            Path candidate = Paths.get(filePath).normalize();
            if (candidate.isAbsolute() && candidate.startsWith(this.baseStorageLocation) && Files.exists(candidate)) {
                return candidate;
            }

            // If relative to baseStorageLocation
            Path relativeCandidate = this.baseStorageLocation.resolve(filePath).normalize();
            if (relativeCandidate.startsWith(this.baseStorageLocation) && Files.exists(relativeCandidate)) {
                return relativeCandidate;
            }

            // Fallback for paths stored with another OS's absolute prefix: search by filename within baseStorageLocation
            Path fileName = candidate.getFileName();
            if (fileName != null && Files.exists(this.baseStorageLocation)) {
                try (Stream<Path> stream = Files.find(this.baseStorageLocation, 5,
                        (p, attrs) -> p.getFileName() != null && p.getFileName().equals(fileName))) {
                    return stream.findFirst().orElse(null);
                }
            }
        } catch (Exception ex) {
            logger.warn("Error resolving file path {}: {}", filePath, ex.getMessage());
        }
        return null;
    }
}
