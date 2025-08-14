package com.ciphergenix.api;

import com.ciphergenix.ai.detection.*;
import com.ciphergenix.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detect")
public class DetectController {

    private final DataPoisoningDetector dataPoisoningDetector;
    private final AdversarialDetector adversarialDetector;

    public DetectController(DataPoisoningDetector dataPoisoningDetector, AdversarialDetector adversarialDetector) {
        this.dataPoisoningDetector = dataPoisoningDetector;
        this.adversarialDetector = adversarialDetector;
    }

    @PostMapping("/poisoning")
    public ResponseEntity<DetectionResult> detectPoisoning(@RequestBody DataPoisoningRequest request) {
        double[][] dataset = convertDataset(request.getDataset());
        DetectionResult result = dataPoisoningDetector.detectPoisoning(dataset);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/adversarial")
    public ResponseEntity<AdversarialDetectionResult> detectAdversarial(@RequestBody AdversarialRequest request) {
        double[] input = request.getInput().stream().mapToDouble(Double::doubleValue).toArray();
        AdversarialDetectionResult result = adversarialDetector.detect(input);
        return ResponseEntity.ok(result);
    }

    private double[][] convertDataset(List<List<Double>> dataset) {
        int rows = dataset.size();
        int cols = dataset.get(0).size();
        double[][] array = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            List<Double> row = dataset.get(i);
            for (int j = 0; j < cols; j++) {
                array[i][j] = row.get(j);
            }
        }
        return array;
    }
}