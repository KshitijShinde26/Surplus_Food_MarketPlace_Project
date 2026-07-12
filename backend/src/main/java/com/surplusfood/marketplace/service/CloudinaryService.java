package com.surplusfood.marketplace.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret
    ) {
        if (cloudName != null && !cloudName.isBlank() &&
                apiKey != null && !apiKey.isBlank() &&
                apiSecret != null && !apiSecret.isBlank()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        } else {
            this.cloudinary = null;
        }
    }

    @SuppressWarnings("rawtypes")
    public Map upload(MultipartFile file) throws IOException {
        if (cloudinary == null) {
            String mockUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500";
            String mockPublicId = "mock-" + UUID.randomUUID();
            return ObjectUtils.asMap(
                    "url", mockUrl,
                    "public_id", mockPublicId
            );
        }
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }

    @SuppressWarnings("rawtypes")
    public Map delete(String publicId) throws IOException {
        if (cloudinary == null || publicId.startsWith("mock-")) {
            return ObjectUtils.asMap("result", "ok");
        }
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
