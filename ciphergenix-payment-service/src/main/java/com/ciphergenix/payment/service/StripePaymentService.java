package com.ciphergenix.payment.service;

import com.ciphergenix.payment.model.Payment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stripe Payment Service
 * 
 * Comprehensive service for integrating with Stripe API.
 * Handles payment processing, customer management, subscriptions,
 * and webhook event processing for the CipherGenix platform.
 */
@Service
public class StripePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.payment.currency:usd}")
    private String defaultCurrency;

    @Value("${stripe.payment.success-url}")
    private String successUrl;

    @Value("${stripe.payment.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe API initialized with secret key");
    }

    /**
     * Create a Payment Intent for processing payments
     */
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String customerId, 
                                           String description, Map<String, String> metadata) throws StripeException {
        
        logger.info("Creating payment intent for customer: {} with amount: {} {}", customerId, amount, currency);

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(amount))
                .setCurrency(currency.toLowerCase())
                .setDescription(description)
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .setConfirm(false);

        // Add customer if provided
        if (customerId != null && !customerId.isEmpty()) {
            paramsBuilder.setCustomer(customerId);
        }

        // Add metadata
        if (metadata != null && !metadata.isEmpty()) {
            paramsBuilder.putAllMetadata(metadata);
        }

        PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());
        
        logger.info("Payment intent created successfully: {}", paymentIntent.getId());
        return paymentIntent;
    }

    /**
     * Confirm a Payment Intent
     */
    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {
        
        logger.info("Confirming payment intent: {} with payment method: {}", paymentIntentId, paymentMethodId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .setReturnUrl(successUrl)
                .build();

        PaymentIntent confirmedIntent = paymentIntent.confirm(params);
        
        logger.info("Payment intent confirmed: {} with status: {}", confirmedIntent.getId(), confirmedIntent.getStatus());
        return confirmedIntent;
    }

    /**
     * Cancel a Payment Intent
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        
        logger.info("Canceling payment intent: {}", paymentIntentId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent canceledIntent = paymentIntent.cancel();
        
        logger.info("Payment intent canceled: {}", canceledIntent.getId());
        return canceledIntent;
    }

    /**
     * Create or retrieve a Stripe customer
     */
    public Customer createOrRetrieveCustomer(String email, String name, Map<String, String> metadata) throws StripeException {
        
        logger.info("Creating or retrieving customer for email: {}", email);

        // First, try to find existing customer by email
        CustomerSearchParams searchParams = CustomerSearchParams.builder()
                .setQuery("email:'" + email + "'")
                .build();

        CustomerSearchResult searchResult = Customer.search(searchParams);
        
        if (!searchResult.getData().isEmpty()) {
            Customer existingCustomer = searchResult.getData().get(0);
            logger.info("Found existing customer: {}", existingCustomer.getId());
            return existingCustomer;
        }

        // Create new customer if not found
        CustomerCreateParams.Builder paramsBuilder = CustomerCreateParams.builder()
                .setEmail(email);

        if (name != null && !name.isEmpty()) {
            paramsBuilder.setName(name);
        }

        if (metadata != null && !metadata.isEmpty()) {
            paramsBuilder.putAllMetadata(metadata);
        }

        Customer customer = Customer.create(paramsBuilder.build());
        
        logger.info("New customer created: {}", customer.getId());
        return customer;
    }

    /**
     * Attach a payment method to a customer
     */
    public PaymentMethod attachPaymentMethod(String paymentMethodId, String customerId) throws StripeException {
        
        logger.info("Attaching payment method: {} to customer: {}", paymentMethodId, customerId);

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        
        PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();

        PaymentMethod attachedMethod = paymentMethod.attach(params);
        
        logger.info("Payment method attached successfully");
        return attachedMethod;
    }

    /**
     * Create a subscription for recurring billing
     */
    public Subscription createSubscription(String customerId, String priceId, int trialPeriodDays) throws StripeException {
        
        logger.info("Creating subscription for customer: {} with price: {}", customerId, priceId);

        SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .setPaymentSettings(SubscriptionCreateParams.PaymentSettings.builder()
                        .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                        .build())
                .setExpandItem("latest_invoice.payment_intent");

        if (trialPeriodDays > 0) {
            paramsBuilder.setTrialPeriodDays((long) trialPeriodDays);
        }

        Subscription subscription = Subscription.create(paramsBuilder.build());
        
        logger.info("Subscription created: {} with status: {}", subscription.getId(), subscription.getStatus());
        return subscription;
    }

    /**
     * Cancel a subscription
     */
    public Subscription cancelSubscription(String subscriptionId, boolean immediately) throws StripeException {
        
        logger.info("Canceling subscription: {} immediately: {}", subscriptionId, immediately);

        Subscription subscription = Subscription.retrieve(subscriptionId);
        
        if (immediately) {
            subscription = subscription.cancel();
        } else {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            subscription = subscription.update(params);
        }
        
        logger.info("Subscription canceled: {}", subscription.getId());
        return subscription;
    }

    /**
     * Create a refund
     */
    public Refund createRefund(String paymentIntentId, BigDecimal amount, String reason) throws StripeException {
        
        logger.info("Creating refund for payment intent: {} amount: {}", paymentIntentId, amount);

        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId);

        if (amount != null) {
            paramsBuilder.setAmount(convertToStripeAmount(amount));
        }

        if (reason != null && !reason.isEmpty()) {
            try {
                RefundCreateParams.Reason reasonEnum = RefundCreateParams.Reason.valueOf(reason.toUpperCase());
                paramsBuilder.setReason(reasonEnum);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid refund reason: {}, using default", reason);
            }
        }

        Refund refund = Refund.create(paramsBuilder.build());
        
        logger.info("Refund created: {} with status: {}", refund.getId(), refund.getStatus());
        return refund;
    }

    /**
     * Create a checkout session for hosted payment page
     */
    public com.stripe.model.checkout.Session createCheckoutSession(String customerId, String priceId, 
                                                                   String successUrl, String cancelUrl, 
                                                                   Map<String, String> metadata) throws StripeException {
        
        logger.info("Creating checkout session for customer: {} with price: {}", customerId, priceId);

        com.stripe.param.checkout.SessionCreateParams.Builder paramsBuilder = 
                com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .setPaymentMethodTypes(List.of("card"))
                .setBillingAddressCollection(com.stripe.param.checkout.SessionCreateParams.BillingAddressCollection.REQUIRED);

        if (metadata != null && !metadata.isEmpty()) {
            paramsBuilder.putAllMetadata(metadata);
        }

        com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(paramsBuilder.build());
        
        logger.info("Checkout session created: {}", session.getId());
        return session;
    }

    /**
     * Retrieve payment method details
     */
    public PaymentMethod getPaymentMethod(String paymentMethodId) throws StripeException {
        
        logger.info("Retrieving payment method: {}", paymentMethodId);
        return PaymentMethod.retrieve(paymentMethodId);
    }

    /**
     * List customer's payment methods
     */
    public List<PaymentMethod> listCustomerPaymentMethods(String customerId, String type) throws StripeException {
        
        logger.info("Listing payment methods for customer: {} type: {}", customerId, type);

        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(PaymentMethodListParams.Type.CARD) // Default to card
                .build();

        PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
        return paymentMethods.getData();
    }

    /**
     * Get invoice details
     */
    public Invoice getInvoice(String invoiceId) throws StripeException {
        
        logger.info("Retrieving invoice: {}", invoiceId);
        return Invoice.retrieve(invoiceId);
    }

    /**
     * Create an invoice
     */
    public Invoice createInvoice(String customerId, boolean autoAdvance) throws StripeException {
        
        logger.info("Creating invoice for customer: {}", customerId);

        InvoiceCreateParams params = InvoiceCreateParams.builder()
                .setCustomer(customerId)
                .setAutoAdvance(autoAdvance)
                .setCollectionMethod(InvoiceCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                .build();

        Invoice invoice = Invoice.create(params);
        
        logger.info("Invoice created: {}", invoice.getId());
        return invoice;
    }

    /**
     * Process webhook events
     */
    public Event processWebhookEvent(String payload, String sigHeader, String endpointSecret) throws StripeException {
        
        logger.info("Processing webhook event");

        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        
        logger.info("Webhook event processed: {} type: {}", event.getId(), event.getType());
        return event;
    }

    /**
     * Get payment intent status
     */
    public Payment.PaymentStatus getPaymentStatusFromStripe(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "requires_payment_method" -> Payment.PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "requires_confirmation" -> Payment.PaymentStatus.REQUIRES_CONFIRMATION;
            case "requires_action" -> Payment.PaymentStatus.REQUIRES_ACTION;
            case "processing" -> Payment.PaymentStatus.PROCESSING;
            case "succeeded" -> Payment.PaymentStatus.SUCCEEDED;
            case "canceled" -> Payment.PaymentStatus.CANCELED;
            default -> Payment.PaymentStatus.FAILED;
        };
    }

    /**
     * Convert BigDecimal amount to Stripe amount (cents)
     */
    private long convertToStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * Convert Stripe amount (cents) to BigDecimal
     */
    public BigDecimal convertFromStripeAmount(long stripeAmount) {
        return BigDecimal.valueOf(stripeAmount).divide(BigDecimal.valueOf(100));
    }

    /**
     * Get default currency
     */
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    /**
     * Validate webhook signature
     */
    public boolean isValidWebhookSignature(String payload, String sigHeader, String endpointSecret) {
        try {
            Webhook.constructEvent(payload, sigHeader, endpointSecret);
            return true;
        } catch (Exception e) {
            logger.warn("Invalid webhook signature: {}", e.getMessage());
            return false;
        }
    }
}