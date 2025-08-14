package com.ciphergenix.alerts;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AlertService {

	public enum Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

	public static class Alert {
		public final Instant timestamp;
		public final Severity severity;
		public final String category;
		public final String message;

		public Alert(Instant timestamp, Severity severity, String category, String message) {
			this.timestamp = timestamp;
			this.severity = severity;
			this.category = category;
			this.message = message;
		}
	}

	private final List<Alert> alerts = Collections.synchronizedList(new ArrayList<>());

	public void raise(Severity severity, String category, String message) {
		alerts.add(new Alert(Instant.now(), severity, category, message));
	}

	public List<Alert> listRecent(int limit) {
		List<Alert> copy;
		synchronized (alerts) {
			copy = new ArrayList<>(alerts);
		}
		int from = Math.max(0, copy.size() - limit);
		return copy.subList(from, copy.size());
	}
}