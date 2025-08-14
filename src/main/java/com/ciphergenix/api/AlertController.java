package com.ciphergenix.api;

import com.ciphergenix.alerts.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

	private final AlertService alertService;

	public AlertController(AlertService alertService) {
		this.alertService = alertService;
	}

	@GetMapping
	public ResponseEntity<?> list(@RequestParam(defaultValue = "50") int limit) {
		return ResponseEntity.ok(alertService.listRecent(Math.max(1, Math.min(500, limit))));
	}
}