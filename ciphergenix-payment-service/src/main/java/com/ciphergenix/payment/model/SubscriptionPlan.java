package com.ciphergenix.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Subscription Plan Entity
 * 
 * Represents subscription plans available in the CipherGenix system.
 * Manages monthly and yearly subscription options with Stripe integration.
 */
@Entity
@Table(name = "subscription_plans", indexes = {
    @Index(name = "idx_plan_stripe_id", columnList = "stripePriceId"),
    @Index(name = "idx_plan_active", columnList = "active"),
    @Index(name = "idx_plan_type", columnList = "planType")
})
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @NotNull
    @Positive
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "stripe_price_id", unique = true)
    private String stripePriceId;

    @Column(name = "stripe_product_id")
    private String stripeProductId;

    @NotNull
    @Column(name = "billing_interval", nullable = false)
    private String billingInterval; // month, year

    @Column(name = "billing_interval_count")
    private Integer billingIntervalCount = 1;

    @Column(name = "trial_period_days")
    private Integer trialPeriodDays = 14;

    @Column(name = "user_limit")
    private Integer userLimit;

    @Column(name = "api_calls_limit")
    private Long apiCallsLimit;

    @Column(name = "storage_limit_gb")
    private Integer storageLimitGb;

    @Column(name = "support_level")
    private String supportLevel;

    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private java.util.Set<String> features;

    @ElementCollection
    @CollectionTable(name = "plan_metadata", joinColumns = @JoinColumn(name = "plan_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public SubscriptionPlan() {}

    public SubscriptionPlan(String name, PlanType planType, BigDecimal price, String billingInterval) {
        this.name = name;
        this.planType = planType;
        this.price = price;
        this.billingInterval = billingInterval;
    }

    // Enums
    public enum PlanType {
        BASIC,
        PROFESSIONAL,
        ENTERPRISE,
        CUSTOM
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public void setStripePriceId(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }

    public String getStripeProductId() {
        return stripeProductId;
    }

    public void setStripeProductId(String stripeProductId) {
        this.stripeProductId = stripeProductId;
    }

    public String getBillingInterval() {
        return billingInterval;
    }

    public void setBillingInterval(String billingInterval) {
        this.billingInterval = billingInterval;
    }

    public Integer getBillingIntervalCount() {
        return billingIntervalCount;
    }

    public void setBillingIntervalCount(Integer billingIntervalCount) {
        this.billingIntervalCount = billingIntervalCount;
    }

    public Integer getTrialPeriodDays() {
        return trialPeriodDays;
    }

    public void setTrialPeriodDays(Integer trialPeriodDays) {
        this.trialPeriodDays = trialPeriodDays;
    }

    public Integer getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(Integer userLimit) {
        this.userLimit = userLimit;
    }

    public Long getApiCallsLimit() {
        return apiCallsLimit;
    }

    public void setApiCallsLimit(Long apiCallsLimit) {
        this.apiCallsLimit = apiCallsLimit;
    }

    public Integer getStorageLimitGb() {
        return storageLimitGb;
    }

    public void setStorageLimitGb(Integer storageLimitGb) {
        this.storageLimitGb = storageLimitGb;
    }

    public String getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(String supportLevel) {
        this.supportLevel = supportLevel;
    }

    public java.util.Set<String> getFeatures() {
        return features;
    }

    public void setFeatures(java.util.Set<String> features) {
        this.features = features;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Utility methods
    public boolean isMonthly() {
        return "month".equals(billingInterval);
    }

    public boolean isYearly() {
        return "year".equals(billingInterval);
    }

    public BigDecimal getMonthlyEquivalentPrice() {
        if (isMonthly()) {
            return price;
        } else if (isYearly()) {
            return price.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        }
        return price;
    }

    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", planType=" + planType +
                ", price=" + price +
                ", billingInterval='" + billingInterval + '\'' +
                ", active=" + active +
                '}';
    }
}