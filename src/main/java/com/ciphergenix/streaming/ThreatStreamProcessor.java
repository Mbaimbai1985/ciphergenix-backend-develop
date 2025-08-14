package com.ciphergenix.streaming;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ThreatStreamProcessor {

	public static class StreamEvent {
		public String type;
		public Map<String, Object> payload;
	}

	public void processBatch(List<StreamEvent> events) {
		// Placeholder for online learning / streaming anomaly detection
		for (StreamEvent e : events) {
			// route to detectors based on type
		}
	}
}