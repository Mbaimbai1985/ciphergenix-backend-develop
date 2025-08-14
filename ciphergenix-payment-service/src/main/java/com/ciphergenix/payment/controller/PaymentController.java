package com.ciphergenix.payment.controller;

import com.ciphergenix.payment.model.Payment;
import com.ciphergenix.payment.service.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment Controller
 * 
 * REST API controller for handling payment operations with Stripe integration.
 * Provides endpoints for payment processing, customer management, subscriptions,
 * and webhook handling for the CipherGenix AI Security Platform.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Service", description = "Payment processing and billing management with Stripe integration")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private StripePaymentService stripePaymentService;

    @Value("${stripe.webhook.endpoint-secret}")
    private String webhookEndpointSecret;

    /**
     * Create a payment intent
     */
    @PostMapping("/payment-intents")
    @Operation(summary = "Create payment intent", 
               description = "Create a Stripe payment intent for processing payments")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        
        logger.info("Creating payment intent for amount: {} {}", request.getAmount(), request.getCurrency());
        
        try {
            PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                request.getAmount(),
                request.getCurrency(),
                request.getCustomerId(),
                request.getDescription(),
                request.getMetadata()
            );

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setId(paymentIntent.getId());
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setAmount(stripePaymentService.convertFromStripeAmount(paymentIntent.getAmount()));
            response.setCurrency(paymentIntent.getCurrency().toUpperCase());
            response.setStatus(paymentIntent.getStatus());
            response.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Payment intent creation failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Confirm a payment intent
     */
    @PostMapping("/payment-intents/{paymentIntentId}/confirm")
    @Operation(summary = "Confirm payment intent", 
               description = "Confirm a payment intent with payment method")
    public ResponseEntity<PaymentIntentResponse> confirmPaymentIntent(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId,
            @Valid @RequestBody ConfirmPaymentIntentRequest request) {
        
        logger.info("Confirming payment intent: {}", paymentIntentId);
        
        try {
            PaymentIntent paymentIntent = stripePaymentService.confirmPaymentIntent(
                paymentIntentId, 
                request.getPaymentMethodId()
            );

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setId(paymentIntent.getId());
            response.setAmount(stripePaymentService.convertFromStripeAmount(paymentIntent.getAmount()));
            response.setCurrency(paymentIntent.getCurrency().toUpperCase());
            response.setStatus(paymentIntent.getStatus());
            response.setConfirmedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error confirming payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Payment confirmation failed: " + e.getMessage()));
        }
    }

    /**
     * Cancel a payment intent
     */
    @PostMapping("/payment-intents/{paymentIntentId}/cancel")
    @Operation(summary = "Cancel payment intent", 
               description = "Cancel a payment intent")
    public ResponseEntity<PaymentIntentResponse> cancelPaymentIntent(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId) {
        
        logger.info("Canceling payment intent: {}", paymentIntentId);
        
        try {
            PaymentIntent paymentIntent = stripePaymentService.cancelPaymentIntent(paymentIntentId);

            PaymentIntentResponse response = new PaymentIntentResponse();
            response.setId(paymentIntent.getId());
            response.setAmount(stripePaymentService.convertFromStripeAmount(paymentIntent.getAmount()));
            response.setCurrency(paymentIntent.getCurrency().toUpperCase());
            response.setStatus(paymentIntent.getStatus());
            response.setCanceledAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error canceling payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Payment cancellation failed: " + e.getMessage()));
        }
    }

    /**
     * Create or retrieve customer
     */
    @PostMapping("/customers")
    @Operation(summary = "Create customer", 
               description = "Create or retrieve a Stripe customer")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        
        logger.info("Creating customer for email: {}", request.getEmail());
        
        try {
            Customer customer = stripePaymentService.createOrRetrieveCustomer(
                request.getEmail(),
                request.getName(),
                request.getMetadata()
            );

            CustomerResponse response = new CustomerResponse();
            response.setId(customer.getId());
            response.setEmail(customer.getEmail());
            response.setName(customer.getName());
            response.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createCustomerErrorResponse("Customer creation failed: " + e.getMessage()));
        }
    }

    /**
     * Create subscription
     */
    @PostMapping("/subscriptions")
    @Operation(summary = "Create subscription", 
               description = "Create a subscription for recurring billing")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        
        logger.info("Creating subscription for customer: {}", request.getCustomerId());
        
        try {
            Subscription subscription = stripePaymentService.createSubscription(
                request.getCustomerId(),
                request.getPriceId(),
                request.getTrialPeriodDays()
            );

            SubscriptionResponse response = new SubscriptionResponse();
            response.setId(subscription.getId());
            response.setCustomerId(subscription.getCustomer());
            response.setStatus(subscription.getStatus());
            response.setCurrentPeriodStart(LocalDateTime.now());
            response.setCurrentPeriodEnd(LocalDateTime.now().plusDays(30));
            response.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error creating subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createSubscriptionErrorResponse("Subscription creation failed: " + e.getMessage()));
        }
    }

    /**
     * Cancel subscription
     */
    @PostMapping("/subscriptions/{subscriptionId}/cancel")
    @Operation(summary = "Cancel subscription", 
               description = "Cancel a subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @Parameter(description = "Subscription ID") @PathVariable String subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediately) {
        
        logger.info("Canceling subscription: {} immediately: {}", subscriptionId, immediately);
        
        try {
            Subscription subscription = stripePaymentService.cancelSubscription(subscriptionId, immediately);

            SubscriptionResponse response = new SubscriptionResponse();
            response.setId(subscription.getId());
            response.setCustomerId(subscription.getCustomer());
            response.setStatus(subscription.getStatus());
            response.setCanceledAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error canceling subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createSubscriptionErrorResponse("Subscription cancellation failed: " + e.getMessage()));
        }
    }

    /**
     * Create refund
     */
    @PostMapping("/refunds")
    @Operation(summary = "Create refund", 
               description = "Create a refund for a payment")
    public ResponseEntity<RefundResponse> createRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        
        logger.info("Creating refund for payment intent: {}", request.getPaymentIntentId());
        
        try {
            Refund refund = stripePaymentService.createRefund(
                request.getPaymentIntentId(),
                request.getAmount(),
                request.getReason()
            );

            RefundResponse response = new RefundResponse();
            response.setId(refund.getId());
            response.setPaymentIntentId(refund.getPaymentIntent());
            response.setAmount(stripePaymentService.convertFromStripeAmount(refund.getAmount()));
            response.setStatus(refund.getStatus());
            response.setReason(refund.getReason());
            response.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error creating refund: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createRefundErrorResponse("Refund creation failed: " + e.getMessage()));
        }
    }

    /**
     * Create checkout session
     */
    @PostMapping("/checkout/sessions")
    @Operation(summary = "Create checkout session", 
               description = "Create a Stripe Checkout session for hosted payment page")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request) {
        
        logger.info("Creating checkout session for customer: {}", request.getCustomerId());
        
        try {
            com.stripe.model.checkout.Session session = stripePaymentService.createCheckoutSession(
                request.getCustomerId(),
                request.getPriceId(),
                request.getSuccessUrl(),
                request.getCancelUrl(),
                request.getMetadata()
            );

            CheckoutSessionResponse response = new CheckoutSessionResponse();
            response.setId(session.getId());
            response.setUrl(session.getUrl());
            response.setCustomerId(session.getCustomer());
            response.setPaymentStatus(session.getPaymentStatus());
            response.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error creating checkout session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createCheckoutErrorResponse("Checkout session creation failed: " + e.getMessage()));
        }
    }

    /**
     * Get customer payment methods
     */
    @GetMapping("/customers/{customerId}/payment-methods")
    @Operation(summary = "Get customer payment methods", 
               description = "Retrieve payment methods for a customer")
    public ResponseEntity<List<PaymentMethodResponse>> getCustomerPaymentMethods(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @RequestParam(defaultValue = "card") String type) {
        
        logger.info("Getting payment methods for customer: {}", customerId);
        
        try {
            List<PaymentMethod> paymentMethods = stripePaymentService.listCustomerPaymentMethods(customerId, type);

            List<PaymentMethodResponse> response = paymentMethods.stream()
                    .map(this::mapPaymentMethodToResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Error getting payment methods: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Webhook endpoint for Stripe events
     */
    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook", 
               description = "Handle Stripe webhook events")
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        logger.info("Received Stripe webhook");
        
        try {
            Event event = stripePaymentService.processWebhookEvent(payload, sigHeader, webhookEndpointSecret);
            
            // Process different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "customer.subscription.created":
                    handleSubscriptionCreated(event);
                    break;
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;
                case "invoice.payment_succeeded":
                    handleInvoicePaymentSucceeded(event);
                    break;
                default:
                    logger.info("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed");
        }
    }

    /**
     * Get subscription plans
     */
    @GetMapping("/plans")
    @Operation(summary = "Get subscription plans", 
               description = "Retrieve all available subscription plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getSubscriptionPlans() {
        
        logger.info("Getting all subscription plans");
        
        // Simulate subscription plans (in real implementation, this would come from database)
        List<SubscriptionPlanResponse> plans = List.of(
            createPlan("basic-monthly", "Basic Monthly", "BASIC", new BigDecimal("29.99"), "month", 
                      "Perfect for small teams getting started", 5, 10000L, 10, "Email"),
            createPlan("basic-yearly", "Basic Yearly", "BASIC", new BigDecimal("299.99"), "year", 
                      "Perfect for small teams getting started (save 17%)", 5, 10000L, 10, "Email"),
            createPlan("professional-monthly", "Professional Monthly", "PROFESSIONAL", new BigDecimal("99.99"), "month", 
                      "Advanced features for growing businesses", 25, 100000L, 100, "Priority"),
            createPlan("professional-yearly", "Professional Yearly", "PROFESSIONAL", new BigDecimal("999.99"), "year", 
                      "Advanced features for growing businesses (save 17%)", 25, 100000L, 100, "Priority"),
            createPlan("enterprise-monthly", "Enterprise Monthly", "ENTERPRISE", new BigDecimal("299.99"), "month", 
                      "Full-scale enterprise security platform", -1, -1L, 1000, "24/7 Phone"),
            createPlan("enterprise-yearly", "Enterprise Yearly", "ENTERPRISE", new BigDecimal("2999.99"), "year", 
                      "Full-scale enterprise security platform (save 17%)", -1, -1L, 1000, "24/7 Phone")
        );
        
        return ResponseEntity.ok(plans);
    }

    /**
     * Get subscription plan by ID
     */
    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get subscription plan", 
               description = "Retrieve a specific subscription plan by ID")
    public ResponseEntity<SubscriptionPlanResponse> getSubscriptionPlan(
            @Parameter(description = "Plan ID") @PathVariable String planId) {
        
        logger.info("Getting subscription plan: {}", planId);
        
        // Simulate getting plan by ID
        List<SubscriptionPlanResponse> plans = getSubscriptionPlans().getBody();
        SubscriptionPlanResponse plan = plans.stream()
                .filter(p -> p.getId().equals(planId))
                .findFirst()
                .orElse(null);
        
        if (plan != null) {
            return ResponseEntity.ok(plan);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check service health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Payment Service");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    // Private helper methods
    private void handlePaymentIntentSucceeded(Event event) {
        logger.info("Processing payment_intent.succeeded event");
        // Implementation for successful payment processing
    }

    private void handlePaymentIntentFailed(Event event) {
        logger.info("Processing payment_intent.payment_failed event");
        // Implementation for failed payment processing
    }

    private void handleSubscriptionCreated(Event event) {
        logger.info("Processing customer.subscription.created event");
        // Implementation for subscription creation
    }

    private void handleSubscriptionUpdated(Event event) {
        logger.info("Processing customer.subscription.updated event");
        // Implementation for subscription updates
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        logger.info("Processing invoice.payment_succeeded event");
        // Implementation for successful invoice payment
    }

    private PaymentMethodResponse mapPaymentMethodToResponse(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.setId(paymentMethod.getId());
        response.setType(paymentMethod.getType());
        response.setCustomerId(paymentMethod.getCustomer());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private PaymentIntentResponse createErrorResponse(String message) {
        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setError(message);
        return response;
    }

    private CustomerResponse createCustomerErrorResponse(String message) {
        CustomerResponse response = new CustomerResponse();
        response.setError(message);
        return response;
    }

    private SubscriptionResponse createSubscriptionErrorResponse(String message) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setError(message);
        return response;
    }

    private RefundResponse createRefundErrorResponse(String message) {
        RefundResponse response = new RefundResponse();
        response.setError(message);
        return response;
    }

    private CheckoutSessionResponse createCheckoutErrorResponse(String message) {
        CheckoutSessionResponse response = new CheckoutSessionResponse();
        response.setError(message);
        return response;
    }

    private SubscriptionPlanResponse createPlan(String id, String name, String planType, BigDecimal price, 
                                              String billingInterval, String description, Integer userLimit, 
                                              Long apiCallsLimit, Integer storageLimit, String supportLevel) {
        SubscriptionPlanResponse plan = new SubscriptionPlanResponse();
        plan.setId(id);
        plan.setName(name);
        plan.setPlanType(planType);
        plan.setPrice(price);
        plan.setBillingInterval(billingInterval);
        plan.setDescription(description);
        plan.setUserLimit(userLimit);
        plan.setApiCallsLimit(apiCallsLimit);
        plan.setStorageLimitGb(storageLimit);
        plan.setSupportLevel(supportLevel);
        plan.setTrialPeriodDays(14);
        plan.setCurrency("USD");
        plan.setActive(true);
        
        // Add features based on plan type
        java.util.Set<String> features = new java.util.HashSet<>();
        switch (planType) {
            case "BASIC":
                features.add("AI Threat Detection");
                features.add("Basic Model Protection");
                features.add("Email Support");
                features.add("Standard Encryption");
                break;
            case "PROFESSIONAL":
                features.add("Advanced AI Threat Detection");
                features.add("Model Integrity Monitoring");
                features.add("Real-time Alerts");
                features.add("API Access");
                features.add("Priority Support");
                features.add("Advanced Encryption");
                features.add("Custom Integrations");
                break;
            case "ENTERPRISE":
                features.add("Enterprise AI Security Suite");
                features.add("Advanced Model Protection");
                features.add("Custom Model Training");
                features.add("Dedicated Support Team");
                features.add("SLA Guarantees");
                features.add("Multi-region Deployment");
                features.add("Advanced Analytics");
                features.add("Custom Reporting");
                features.add("SSO Integration");
                break;
        }
        plan.setFeatures(features);
        
        return plan;
    }

    // Request/Response DTOs
    public static class CreatePaymentIntentRequest {
        private BigDecimal amount;
        private String currency = "USD";
        private String customerId;
        private String description;
        private Map<String, String> metadata;

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }

    public static class ConfirmPaymentIntentRequest {
        private String paymentMethodId;

        public String getPaymentMethodId() { return paymentMethodId; }
        public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    }

    public static class CreateCustomerRequest {
        private String email;
        private String name;
        private Map<String, String> metadata;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }

    public static class CreateSubscriptionRequest {
        private String customerId;
        private String priceId;
        private int trialPeriodDays = 0;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getPriceId() { return priceId; }
        public void setPriceId(String priceId) { this.priceId = priceId; }
        
        public int getTrialPeriodDays() { return trialPeriodDays; }
        public void setTrialPeriodDays(int trialPeriodDays) { this.trialPeriodDays = trialPeriodDays; }
    }

    public static class CreateRefundRequest {
        private String paymentIntentId;
        private BigDecimal amount;
        private String reason;

        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CreateCheckoutSessionRequest {
        private String customerId;
        private String priceId;
        private String successUrl;
        private String cancelUrl;
        private Map<String, String> metadata;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getPriceId() { return priceId; }
        public void setPriceId(String priceId) { this.priceId = priceId; }
        
        public String getSuccessUrl() { return successUrl; }
        public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
        
        public String getCancelUrl() { return cancelUrl; }
        public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }

    // Response DTOs
    public static class PaymentIntentResponse {
        private String id;
        private String clientSecret;
        private BigDecimal amount;
        private String currency;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime canceledAt;
        private String error;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
        
        public LocalDateTime getCanceledAt() { return canceledAt; }
        public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class CustomerResponse {
        private String id;
        private String email;
        private String name;
        private LocalDateTime createdAt;
        private String error;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SubscriptionResponse {
        private String id;
        private String customerId;
        private String status;
        private LocalDateTime currentPeriodStart;
        private LocalDateTime currentPeriodEnd;
        private LocalDateTime createdAt;
        private LocalDateTime canceledAt;
        private String error;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCurrentPeriodStart() { return currentPeriodStart; }
        public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
        
        public LocalDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
        public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getCanceledAt() { return canceledAt; }
        public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class RefundResponse {
        private String id;
        private String paymentIntentId;
        private BigDecimal amount;
        private String status;
        private String reason;
        private LocalDateTime createdAt;
        private String error;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class CheckoutSessionResponse {
        private String id;
        private String url;
        private String customerId;
        private String paymentStatus;
        private LocalDateTime createdAt;
        private String error;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class PaymentMethodResponse {
        private String id;
        private String type;
        private String customerId;
        private LocalDateTime createdAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class SubscriptionPlanResponse {
        private String id;
        private String name;
        private String description;
        private String planType;
        private BigDecimal price;
        private String currency;
        private String billingInterval;
        private Integer trialPeriodDays;
        private Integer userLimit;
        private Long apiCallsLimit;
        private Integer storageLimitGb;
        private String supportLevel;
        private java.util.Set<String> features;
        private Boolean active;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getBillingInterval() { return billingInterval; }
        public void setBillingInterval(String billingInterval) { this.billingInterval = billingInterval; }
        
        public Integer getTrialPeriodDays() { return trialPeriodDays; }
        public void setTrialPeriodDays(Integer trialPeriodDays) { this.trialPeriodDays = trialPeriodDays; }
        
        public Integer getUserLimit() { return userLimit; }
        public void setUserLimit(Integer userLimit) { this.userLimit = userLimit; }
        
        public Long getApiCallsLimit() { return apiCallsLimit; }
        public void setApiCallsLimit(Long apiCallsLimit) { this.apiCallsLimit = apiCallsLimit; }
        
        public Integer getStorageLimitGb() { return storageLimitGb; }
        public void setStorageLimitGb(Integer storageLimitGb) { this.storageLimitGb = storageLimitGb; }
        
        public String getSupportLevel() { return supportLevel; }
        public void setSupportLevel(String supportLevel) { this.supportLevel = supportLevel; }
        
        public java.util.Set<String> getFeatures() { return features; }
        public void setFeatures(java.util.Set<String> features) { this.features = features; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}