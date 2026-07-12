package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.service.CloudinaryService;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    @SuppressWarnings("rawtypes")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public Map uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return cloudinaryService.upload(file);
    }
}
