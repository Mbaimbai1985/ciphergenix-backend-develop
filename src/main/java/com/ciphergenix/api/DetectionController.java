package com.ciphergenix.api;

import com.ciphergenix.alerts.AlertService;
import com.ciphergenix.api.dto.DetectionRequests;
import com.ciphergenix.detection.adversarial.BasicAdversarialDetector;
import com.ciphergenix.detection.poisoning.BasicEnsemblePoisoningDetector;
import com.ciphergenix.monitoring.ModelIntegrityService;
import com.ciphergenix.security.SecureDataPipeline;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DetectionController {

	private final BasicEnsemblePoisoningDetector dataPoisoningDetector;
	private final BasicAdversarialDetector adversarialDetector;
	private final SecureDataPipeline secureDataPipeline;
	private final ModelIntegrityService modelIntegrityService;
	private final AlertService alertService;

	public DetectionController(BasicEnsemblePoisoningDetector dataPoisoningDetector,
							  BasicAdversarialDetector adversarialDetector,
							  SecureDataPipeline secureDataPipeline,
							  ModelIntegrityService modelIntegrityService,
							  AlertService alertService) {
		this.dataPoisoningDetector = dataPoisoningDetector;
		this.adversarialDetector = adversarialDetector;
		this.secureDataPipeline = secureDataPipeline;
		this.modelIntegrityService = modelIntegrityService;
		this.alertService = alertService;
	}

	@PostMapping("/detect/poisoning")
	public ResponseEntity<DetectionRequests.PoisoningDetectResponse> detectPoisoning(
			@Valid @RequestBody DetectionRequests.PoisoningDetectRequest request) {
		var result = dataPoisoningDetector.detectPoisoning(request.dataset, request.baselineStats);
		if (result.threatScore >= 0.7) {
			alertService.raise(AlertService.Severity.HIGH, "data_poisoning", "High poisoning threat score: " + result.threatScore);
		}
		var response = new DetectionRequests.PoisoningDetectResponse();
		response.threatScore = result.threatScore;
		response.anomalousSampleIndices = result.anomalousSampleIndices;
		return ResponseEntity.ok(response);
	}

	@PostMapping("/detect/adversarial")
	public ResponseEntity<DetectionRequests.AdversarialDetectResponse> detectAdversarial(
			@Valid @RequestBody DetectionRequests.AdversarialDetectRequest request) {
		var result = adversarialDetector.detectAdversarial(request.inputFeatures);
		if (result.isAdversarial) {
			alertService.raise(AlertService.Severity.MEDIUM, "adversarial", "Adversarial input detected, score=" + result.confidenceScore);
		}
		var response = new DetectionRequests.AdversarialDetectResponse();
		response.adversarial = result.isAdversarial;
		response.confidence = result.confidenceScore;
		return ResponseEntity.ok(response);
	}

	@PostMapping("/pipeline/ingest")
	public ResponseEntity<DetectionRequests.IngestResponse> secureIngest(
			@Valid @RequestBody DetectionRequests.IngestRequest request) throws Exception {
		var secured = secureDataPipeline.secureDataIngestion(request.data, request.role);
		var response = new DetectionRequests.IngestResponse();
		response.ciphertextBase64 = secured.ciphertextBase64;
		response.ivBase64 = secured.ivBase64;
		response.hmacBase64 = secured.hmacBase64;
		return ResponseEntity.ok(response);
	}

	@PostMapping("/monitoring/prediction")
	public ResponseEntity<Void> recordPrediction(@Valid @RequestBody DetectionRequests.PredictionRecordRequest request) {
		modelIntegrityService.recordPrediction(request.outputDistribution, request.latencyMs);
		return ResponseEntity.accepted().build();
	}

	@GetMapping("/monitoring/status")
	public ResponseEntity<DetectionRequests.MonitoringStatusResponse> monitoringStatus() {
		var response = new DetectionRequests.MonitoringStatusResponse();
		response.driftScore = modelIntegrityService.computeDriftScore();
		response.performanceAnomalyScore = modelIntegrityService.computePerformanceAnomalyScore();
		response.behaviorFingerprint = modelIntegrityService.fingerprintModelBehavior();
		return ResponseEntity.ok(response);
	}
}