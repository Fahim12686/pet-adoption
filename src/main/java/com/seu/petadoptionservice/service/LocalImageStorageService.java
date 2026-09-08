package com.seu.petadoptionservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Saves uploaded images into src/main/resources/static/images (see
 * app.upload.dir) and returns a URL path the app serves them back from at
 * /images/**. Because this is a location inside the source tree rather than
 * a temp/runtime folder, uploaded images persist across app restarts and
 * rebuilds during local development - they're just ordinary files sitting
 * in the repo. You can also drop images in manually via your file explorer
 * or IDE and reference them the same way.
 *
 * Deployment considerations (cloud storage, etc.) are out of scope for now.
 */
@Service
public class LocalImageStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Saves the file into a subfolder (e.g. "pets", "store") under the
     * configured upload directory, and returns the public URL path
     * (e.g. "/images/pets/&lt;uuid&gt;.jpg") to store on the entity.
     * Returns null if no file was provided.
     *
     * @throws RuntimeException if the file can't be saved (disk error, etc.)
     */
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID() + extension;

        try {
            Path targetDir = Paths.get(uploadDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(filename);
            try (var in = file.getInputStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/images/" + folder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded image to disk: " + e.getMessage(), e);
        }
    }
}
