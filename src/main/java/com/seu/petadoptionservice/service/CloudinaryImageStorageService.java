package com.seu.petadoptionservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Uploads images to Cloudinary and returns the permanent public HTTPS URL
 * Cloudinary gives back. Unlike writing to local disk, this survives
 * redeploys/restarts on platforms with an ephemeral filesystem (e.g. Render's
 * free tier), since the file itself lives on Cloudinary, not in the
 * container.
 */
@Service
public class CloudinaryImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Uploads the file into a Cloudinary folder (e.g. "pets", "store") and
     * returns the public HTTPS URL to store on the entity.
     * Returns null if no file was provided.
     *
     * @throws RuntimeException if the upload fails (network error, bad credentials, etc.)
     */
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String publicId = "pet-adoption/" + folder + "/" + UUID.randomUUID();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image"
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
}
