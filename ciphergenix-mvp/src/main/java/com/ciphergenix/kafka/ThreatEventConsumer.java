package com.ciphergenix.kafka;

import com.ciphergenix.dto.DetectionResponse;
import com.ciphergenix.security.engine.RealTimeThreatMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThreatEventConsumer {
    
    @Autowired
    private RealTimeThreatMonitor realTimeThreatMonitor;
    
    @KafkaListener(
        topics = "${kafka.topics.threat-detection}",
        containerFactory = "detectionKafkaListenerContainerFactory"
    )
    public void consumeDetectionEvent(
            @Payload DetectionResponse detection,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received detection event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            
            // Process the detection event
            realTimeThreatMonitor.processThreatDetection(detection);
            
            // Acknowledge message after successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing detection event", e);
            // In production, implement retry logic or dead letter queue
        }
    }
    
    @KafkaListener(
        topics = "threat-detection-topic",
        containerFactory = "threatEventKafkaListenerContainerFactory"
    )
    public void consumeThreatEvent(
            @Payload RealTimeThreatMonitor.ThreatEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            log.debug("Received threat event: {} from topic: {}", event.getEventId(), topic);
            
            // Process distributed threat event
            // This would be used in a multi-instance deployment
            
        } catch (Exception e) {
            log.error("Error processing threat event", e);
        }
    }
}