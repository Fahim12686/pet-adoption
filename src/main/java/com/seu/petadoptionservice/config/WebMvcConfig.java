package com.seu.petadoptionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * As of Phase 2.5, images are saved directly into
 * src/main/resources/static/images (see LocalImageStorageService), which
 * Spring Boot serves automatically at /images/** because anything under
 * the classpath "static/" folder is exposed by default. This class is kept
 * as a safety net: if UPLOAD_DIR is ever overridden to point somewhere
 * outside static/ (e.g. during local experimentation), this handler makes
 * sure /images/** still resolves to wherever app.upload.dir actually points.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}
