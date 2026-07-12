package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.ChatbotRequest;
import com.surplusfood.marketplace.dto.ChatbotResponse;
import com.surplusfood.marketplace.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/extract")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ChatbotResponse> extractInformation(
            @Valid @RequestBody ChatbotRequest request
    ) {
        ChatbotResponse response = chatbotService.extractInformation(request.description());
        return ResponseEntity.ok(response);
    }
}
