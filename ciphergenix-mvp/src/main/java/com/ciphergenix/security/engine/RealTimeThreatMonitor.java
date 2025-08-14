package com.ciphergenix.security.engine;

import com.ciphergenix.dto.DetectionResponse;
import com.ciphergenix.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RealTimeThreatMonitor {
    
    private final ConcurrentHashMap<String, ThreatEvent> activeThreatEvents = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<ThreatEvent> threatEventQueue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ExecutorService alertExecutor = Executors.newFixedThreadPool(2);
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final ThreatAggregator threatAggregator;
    private final AlertSystem alertSystem;
    private final OnlineLearningEngine onlineLearning;
    
    public RealTimeThreatMonitor() {
        this.threatAggregator = new ThreatAggregator();
        this.alertSystem = new AlertSystem();
        this.onlineLearning = new OnlineLearningEngine();
        
        // Start background processors
        startThreatProcessor();
        startAggregationScheduler();
        startCleanupScheduler();
    }
    
    @Async
    public void processThreatDetection(DetectionResponse detection) {
        try {
            ThreatEvent event = ThreatEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .detectionId(detection.getDetectionId())
                .threatType(detection.getDetectionType())
                .threatLevel(detection.getThreatLevel())
                .threatScore(detection.getThreatScore())
                .timestamp(detection.getTimestamp())
                .details(detection.getDetectionDetails())
                .build();
            
            // Add to processing queue
            if (!threatEventQueue.offer(event)) {
                log.warn("Threat event queue full, dropping event: {}", event.getEventId());
            }
            
            // Process immediate high-priority threats
            if (event.getThreatLevel() == ThreatLevel.CRITICAL || 
                event.getThreatLevel() == ThreatLevel.HIGH) {
                processHighPriorityThreat(event);
            }
            
            // Send to Kafka for distributed processing
            if (kafkaTemplate != null) {
                kafkaTemplate.send("threat-detection-topic", event);
            }
            
        } catch (Exception e) {
            log.error("Error processing threat detection", e);
        }
    }
    
    private void processHighPriorityThreat(ThreatEvent event) {
        alertExecutor.submit(() -> {
            try {
                // Immediate alert for critical threats
                Alert alert = Alert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .severity(mapThreatLevelToSeverity(event.getThreatLevel()))
                    .title("High Priority Threat Detected: " + event.getThreatType())
                    .description(String.format(
                        "Threat Score: %.2f, Detection ID: %s",
                        event.getThreatScore(), event.getDetectionId()
                    ))
                    .timestamp(LocalDateTime.now())
                    .actions(generateRecommendedActions(event))
                    .build();
                
                alertSystem.sendAlert(alert);
                
                // Update active threats
                activeThreatEvents.put(event.getEventId(), event);
                
            } catch (Exception e) {
                log.error("Error processing high priority threat", e);
            }
        });
    }
    
    private void startThreatProcessor() {
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ThreatEvent event = threatEventQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        processThreatEvent(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in threat processor", e);
                }
            }
        });
    }
    
    private void processThreatEvent(ThreatEvent event) {
        // Update online learning model
        onlineLearning.updateModel(event);
        
        // Aggregate threat patterns
        threatAggregator.addEvent(event);
        
        // Check for attack patterns
        List<AttackPattern> patterns = detectAttackPatterns(event);
        if (!patterns.isEmpty()) {
            handleAttackPatterns(patterns);
        }
        
        // Store in active threats
        activeThreatEvents.put(event.getEventId(), event);
    }
    
    private void startAggregationScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ThreatSummary summary = threatAggregator.generateSummary();
                
                // Send summary to Kafka
                if (kafkaTemplate != null) {
                    kafkaTemplate.send("threat-summary-topic", summary);
                }
                
                // Check for anomalous patterns
                if (summary.hasAnomalousActivity()) {
                    Alert alert = Alert.builder()
                        .alertId(UUID.randomUUID().toString())
                        .severity(AlertSeverity.WARNING)
                        .title("Anomalous Threat Activity Detected")
                        .description(summary.getAnomalyDescription())
                        .timestamp(LocalDateTime.now())
                        .build();
                    
                    alertSystem.sendAlert(alert);
                }
                
            } catch (Exception e) {
                log.error("Error in aggregation scheduler", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
    
    private void startCleanupScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime cutoff = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
                
                // Remove old events
                activeThreatEvents.entrySet().removeIf(entry -> 
                    entry.getValue().getTimestamp().isBefore(cutoff)
                );
                
                // Clean aggregator
                threatAggregator.cleanOldData(cutoff);
                
            } catch (Exception e) {
                log.error("Error in cleanup scheduler", e);
            }
        }, 0, 1, TimeUnit.HOURS);
    }
    
    private List<AttackPattern> detectAttackPatterns(ThreatEvent event) {
        List<AttackPattern> patterns = new ArrayList<>();
        
        // Check for coordinated attacks
        List<ThreatEvent> recentEvents = getRecentEvents(5, ChronoUnit.MINUTES);
        
        // Pattern 1: Multiple similar threats from same source
        Map<String, List<ThreatEvent>> byType = recentEvents.stream()
            .collect(Collectors.groupingBy(ThreatEvent::getThreatType));
        
        for (Map.Entry<String, List<ThreatEvent>> entry : byType.entrySet()) {
            if (entry.getValue().size() >= 5) {
                patterns.add(new AttackPattern(
                    "REPEATED_ATTACK",
                    entry.getKey(),
                    entry.getValue().size()
                ));
            }
        }
        
        // Pattern 2: Escalating threat levels
        boolean escalation = checkEscalation(recentEvents);
        if (escalation) {
            patterns.add(new AttackPattern(
                "ESCALATING_ATTACK",
                "Multiple threat types",
                recentEvents.size()
            ));
        }
        
        // Pattern 3: Distributed attack pattern
        if (checkDistributedPattern(recentEvents)) {
            patterns.add(new AttackPattern(
                "DISTRIBUTED_ATTACK",
                "Multiple sources",
                recentEvents.size()
            ));
        }
        
        return patterns;
    }
    
    private boolean checkEscalation(List<ThreatEvent> events) {
        if (events.size() < 3) return false;
        
        List<ThreatEvent> sorted = events.stream()
            .sorted(Comparator.comparing(ThreatEvent::getTimestamp))
            .collect(Collectors.toList());
        
        int escalationCount = 0;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).getThreatLevel().ordinal() > 
                sorted.get(i-1).getThreatLevel().ordinal()) {
                escalationCount++;
            }
        }
        
        return escalationCount >= sorted.size() / 2;
    }
    
    private boolean checkDistributedPattern(List<ThreatEvent> events) {
        // Check if threats are coming from multiple sources in a short time
        Set<String> sources = new HashSet<>();
        for (ThreatEvent event : events) {
            if (event.getDetails() != null && event.getDetails().containsKey("sourceId")) {
                sources.add(event.getDetails().get("sourceId").toString());
            }
        }
        
        return sources.size() >= 3 && events.size() >= 5;
    }
    
    private void handleAttackPatterns(List<AttackPattern> patterns) {
        for (AttackPattern pattern : patterns) {
            Alert alert = Alert.builder()
                .alertId(UUID.randomUUID().toString())
                .severity(AlertSeverity.CRITICAL)
                .title("Attack Pattern Detected: " + pattern.getPatternType())
                .description(String.format(
                    "Pattern: %s, Target: %s, Count: %d",
                    pattern.getPatternType(), pattern.getTarget(), pattern.getCount()
                ))
                .timestamp(LocalDateTime.now())
                .actions(Arrays.asList(
                    "Activate defensive measures",
                    "Increase monitoring sensitivity",
                    "Review recent threat events"
                ))
                .build();
            
            alertSystem.sendAlert(alert);
        }
    }
    
    private List<ThreatEvent> getRecentEvents(int amount, ChronoUnit unit) {
        LocalDateTime cutoff = LocalDateTime.now().minus(amount, unit);
        return activeThreatEvents.values().stream()
            .filter(event -> event.getTimestamp().isAfter(cutoff))
            .collect(Collectors.toList());
    }
    
    private AlertSeverity mapThreatLevelToSeverity(ThreatLevel level) {
        switch (level) {
            case CRITICAL:
                return AlertSeverity.CRITICAL;
            case HIGH:
                return AlertSeverity.HIGH;
            case MEDIUM:
                return AlertSeverity.WARNING;
            case LOW:
            default:
                return AlertSeverity.INFO;
        }
    }
    
    private List<String> generateRecommendedActions(ThreatEvent event) {
        List<String> actions = new ArrayList<>();
        
        switch (event.getThreatType()) {
            case "DATA_POISONING":
                actions.add("Quarantine affected dataset");
                actions.add("Validate data integrity");
                actions.add("Review data ingestion logs");
                break;
            case "ADVERSARIAL_ATTACK":
                actions.add("Enable input sanitization");
                actions.add("Activate defensive transformations");
                actions.add("Increase detection sensitivity");
                break;
            case "MODEL_INTEGRITY_MONITORING":
                actions.add("Suspend model deployment");
                actions.add("Compare with baseline model");
                actions.add("Review recent model updates");
                break;
            default:
                actions.add("Review threat details");
                actions.add("Monitor for similar threats");
        }
        
        return actions;
    }
    
    // Inner classes
    public static class ThreatEvent {
        private String eventId;
        private String detectionId;
        private String threatType;
        private ThreatLevel threatLevel;
        private Double threatScore;
        private LocalDateTime timestamp;
        private Map<String, Object> details;
        
        public static ThreatEventBuilder builder() {
            return new ThreatEventBuilder();
        }
        
        public String getEventId() { return eventId; }
        public String getThreatType() { return threatType; }
        public ThreatLevel getThreatLevel() { return threatLevel; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getDetails() { return details; }
        public Double getThreatScore() { return threatScore; }
        public String getDetectionId() { return detectionId; }
        
        public static class ThreatEventBuilder {
            private ThreatEvent event = new ThreatEvent();
            
            public ThreatEventBuilder eventId(String eventId) {
                event.eventId = eventId;
                return this;
            }
            
            public ThreatEventBuilder detectionId(String detectionId) {
                event.detectionId = detectionId;
                return this;
            }
            
            public ThreatEventBuilder threatType(String threatType) {
                event.threatType = threatType;
                return this;
            }
            
            public ThreatEventBuilder threatLevel(ThreatLevel threatLevel) {
                event.threatLevel = threatLevel;
                return this;
            }
            
            public ThreatEventBuilder threatScore(Double threatScore) {
                event.threatScore = threatScore;
                return this;
            }
            
            public ThreatEventBuilder timestamp(LocalDateTime timestamp) {
                event.timestamp = timestamp;
                return this;
            }
            
            public ThreatEventBuilder details(Map<String, Object> details) {
                event.details = details;
                return this;
            }
            
            public ThreatEvent build() {
                return event;
            }
        }
    }
    
    private static class ThreatAggregator {
        private final Map<String, AtomicInteger> threatCounts = new ConcurrentHashMap<>();
        private final Map<String, Double> threatScores = new ConcurrentHashMap<>();
        private final List<ThreatEvent> recentEvents = new CopyOnWriteArrayList<>();
        
        public void addEvent(ThreatEvent event) {
            threatCounts.computeIfAbsent(event.getThreatType(), k -> new AtomicInteger())
                .incrementAndGet();
            
            threatScores.merge(event.getThreatType(), event.getThreatScore(), Double::sum);
            
            recentEvents.add(event);
            if (recentEvents.size() > 1000) {
                recentEvents.remove(0);
            }
        }
        
        public ThreatSummary generateSummary() {
            Map<String, Integer> counts = new HashMap<>();
            threatCounts.forEach((k, v) -> counts.put(k, v.get()));
            
            Map<String, Double> avgScores = new HashMap<>();
            threatScores.forEach((type, totalScore) -> {
                int count = counts.getOrDefault(type, 1);
                avgScores.put(type, totalScore / count);
            });
            
            return new ThreatSummary(counts, avgScores, detectAnomalies());
        }
        
        private boolean detectAnomalies() {
            // Simple anomaly detection based on sudden spikes
            if (recentEvents.size() < 10) return false;
            
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
            long recentCount = recentEvents.stream()
                .filter(e -> e.getTimestamp().isAfter(fiveMinutesAgo))
                .count();
            
            return recentCount > recentEvents.size() * 0.5;
        }
        
        public void cleanOldData(LocalDateTime cutoff) {
            recentEvents.removeIf(event -> event.getTimestamp().isBefore(cutoff));
        }
    }
    
    private static class ThreatSummary {
        private final Map<String, Integer> threatCounts;
        private final Map<String, Double> averageScores;
        private final boolean anomalousActivity;
        
        public ThreatSummary(Map<String, Integer> threatCounts, 
                           Map<String, Double> averageScores,
                           boolean anomalousActivity) {
            this.threatCounts = threatCounts;
            this.averageScores = averageScores;
            this.anomalousActivity = anomalousActivity;
        }
        
        public boolean hasAnomalousActivity() {
            return anomalousActivity;
        }
        
        public String getAnomalyDescription() {
            return "Unusual spike in threat activity detected. " +
                   "Threat count increased significantly in the last 5 minutes.";
        }
    }
    
    private static class AlertSystem {
        private final List<Alert> alertHistory = new CopyOnWriteArrayList<>();
        
        public void sendAlert(Alert alert) {
            alertHistory.add(alert);
            
            // Log alert
            log.warn("SECURITY ALERT [{}]: {} - {}", 
                    alert.getSeverity(), alert.getTitle(), alert.getDescription());
            
            // In production, would send to:
            // - Email notification service
            // - SMS/PagerDuty for critical alerts
            // - Slack/Teams webhooks
            // - SIEM integration
        }
    }
    
    private static class OnlineLearningEngine {
        // Simplified online learning for threat pattern detection
        private final Map<String, Double> threatWeights = new ConcurrentHashMap<>();
        
        public void updateModel(ThreatEvent event) {
            // Update weights based on threat severity
            double weight = event.getThreatLevel().ordinal() * 0.25;
            threatWeights.merge(event.getThreatType(), weight, (old, new_) -> old * 0.9 + new_ * 0.1);
        }
    }
    
    private static class AttackPattern {
        private final String patternType;
        private final String target;
        private final int count;
        
        public AttackPattern(String patternType, String target, int count) {
            this.patternType = patternType;
            this.target = target;
            this.count = count;
        }
        
        public String getPatternType() { return patternType; }
        public String getTarget() { return target; }
        public int getCount() { return count; }
    }
    
    public static class Alert {
        private String alertId;
        private AlertSeverity severity;
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private List<String> actions;
        
        public static AlertBuilder builder() {
            return new AlertBuilder();
        }
        
        public AlertSeverity getSeverity() { return severity; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        
        public static class AlertBuilder {
            private Alert alert = new Alert();
            
            public AlertBuilder alertId(String alertId) {
                alert.alertId = alertId;
                return this;
            }
            
            public AlertBuilder severity(AlertSeverity severity) {
                alert.severity = severity;
                return this;
            }
            
            public AlertBuilder title(String title) {
                alert.title = title;
                return this;
            }
            
            public AlertBuilder description(String description) {
                alert.description = description;
                return this;
            }
            
            public AlertBuilder timestamp(LocalDateTime timestamp) {
                alert.timestamp = timestamp;
                return this;
            }
            
            public AlertBuilder actions(List<String> actions) {
                alert.actions = actions;
                return this;
            }
            
            public Alert build() {
                return alert;
            }
        }
    }
    
    public enum AlertSeverity {
        INFO, WARNING, HIGH, CRITICAL
    }
}