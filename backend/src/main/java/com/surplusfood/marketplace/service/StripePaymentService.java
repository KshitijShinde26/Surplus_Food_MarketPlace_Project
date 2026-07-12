package com.surplusfood.marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.surplusfood.marketplace.dto.PaymentIntentResponse;
import com.surplusfood.marketplace.entity.Order;
import com.surplusfood.marketplace.entity.OrderStatus;
import com.surplusfood.marketplace.entity.Payment;
import com.surplusfood.marketplace.entity.PaymentStatus;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.repository.OrderRepository;
import com.surplusfood.marketplace.repository.PaymentRepository;
import com.surplusfood.marketplace.entity.NotificationType;
import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final PickupScheduleService pickupScheduleService;
    private final TransactionService transactionService;

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        } else {
            log.warn("Stripe API key is not configured. Payment services will run in Sandbox Mock Mode.");
        }
    }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is not in PENDING_PAYMENT status");
        }

        BigDecimal amount = order.getTotalAmount();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            String clientSecret;
            String stripeId;

            if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("usd")
                        .putMetadata("orderId", String.valueOf(orderId))
                        .build();

                PaymentIntent intent = PaymentIntent.create(params);
                clientSecret = intent.getClientSecret();
                stripeId = intent.getId();
            } else {
                clientSecret = "mock_client_secret_" + UUID.randomUUID();
                stripeId = "mock_pi_" + UUID.randomUUID();
            }

            Payment payment = paymentRepository.findByOrderId(orderId).orElseGet(Payment::new);
            payment.setOrder(order);
            payment.setStripePaymentIntentId(stripeId);
            payment.setAmount(amount);
            payment.setCurrency("usd");
            payment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
            paymentRepository.save(payment);

            return new PaymentIntentResponse(clientSecret, stripeId, orderId, amount);

        } catch (Exception e) {
            log.error("Failed to create Stripe PaymentIntent", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe PaymentIntent creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void processWebhookEvent(String payload, String sigHeader) {
        log.info("Processing Stripe webhook event. SigHeader present: {}", sigHeader != null);

        try {
            @SuppressWarnings("rawtypes")
            Map eventMap = objectMapper.readValue(payload, Map.class);
            String type = (String) eventMap.get("type");

            if (type == null) {
                log.warn("Invalid webhook payload, type is missing");
                return;
            }

            log.info("Received Stripe webhook event type: {}", type);

            if ("payment_intent.succeeded".equals(type) || "payment_intent.payment_failed".equals(type)) {
                @SuppressWarnings("rawtypes")
                Map dataMap = (Map) eventMap.get("data");
                @SuppressWarnings("rawtypes")
                Map objectMap = (Map) dataMap.get("object");
                String stripeId = (String) objectMap.get("id");

                PaymentStatus paymentStatus = "payment_intent.succeeded".equals(type) ?
                        PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;

                Payment payment = paymentRepository.findByStripePaymentIntentId(stripeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for intent: " + stripeId));

                payment.setStatus(paymentStatus);
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                if (paymentStatus == PaymentStatus.SUCCEEDED) {
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);

                    notificationService.sendNotification(
                            order.getConsumer(),
                            "Order Payment Succeeded",
                            "Your payment was successful. Order is confirmed. Pickup is scheduled at: " + order.getListing().getPickupStartTime(),
                            NotificationType.PAYMENT_UPDATE
                    );
                    notificationService.sendNotification(
                            order.getListing().getBusiness().getOwner(),
                            "Order Paid & Confirmed",
                            "Consumer " + order.getConsumer().getFullName() + " has paid for Order #" + order.getId() + ".",
                            NotificationType.ORDER_ACCEPTED
                    );

                    pickupScheduleService.createScheduleForOrder(order, order.getListing().getPickupStartTime());
                    
                    transactionService.logTransaction(
                            order.getListing().getBusiness(),
                            order,
                            null,
                            TransactionType.SALE,
                            order.getTotalAmount(),
                            TransactionStatus.SUCCESS
                    );
                } else {
                    order.setStatus(OrderStatus.FAILED);
                    orderRepository.save(order);
                    
                    transactionService.logTransaction(
                            order.getListing().getBusiness(),
                            order,
                            null,
                            TransactionType.SALE,
                            order.getTotalAmount(),
                            TransactionStatus.FAILED
                    );
                }
                log.info("Updated order {} status to {}", order.getId(), order.getStatus());
            }

        } catch (Exception e) {
            log.error("Stripe webhook processing error", e);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook processing failed: " + e.getMessage());
        }
    }
}
