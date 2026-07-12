package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.PaymentIntentResponse;
import com.surplusfood.marketplace.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/create-intent/{orderId}")
    @PreAuthorize("hasRole('CONSUMER')")
    public PaymentIntentResponse createPaymentIntent(@PathVariable Long orderId) {
        return stripePaymentService.createPaymentIntent(orderId);
    }

    @PostMapping("/webhook")
    public void stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        stripePaymentService.processWebhookEvent(payload, sigHeader);
    }
}
