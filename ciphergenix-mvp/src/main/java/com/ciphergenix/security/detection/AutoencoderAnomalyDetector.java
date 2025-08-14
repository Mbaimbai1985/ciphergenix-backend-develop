package com.ciphergenix.security.detection;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AutoencoderAnomalyDetector {
    
    private MultiLayerNetwork autoencoder;
    private double reconstructionThreshold = 0.1;
    
    public MultiLayerNetwork buildAutoencoder(int inputSize) {
        int[] layerSizes = {inputSize, inputSize / 2, inputSize / 4, inputSize / 2, inputSize};
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12345)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(0, new DenseLayer.Builder()
                .nIn(layerSizes[0])
                .nOut(layerSizes[1])
                .activation(Activation.RELU)
                .build())
            .layer(1, new DenseLayer.Builder()
                .nIn(layerSizes[1])
                .nOut(layerSizes[2])
                .activation(Activation.RELU)
                .build())
            .layer(2, new DenseLayer.Builder()
                .nIn(layerSizes[2])
                .nOut(layerSizes[3])
                .activation(Activation.RELU)
                .build())
            .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(layerSizes[3])
                .nOut(layerSizes[4])
                .activation(Activation.SIGMOID)
                .build())
            .build();
        
        autoencoder = new MultiLayerNetwork(conf);
        autoencoder.init();
        
        return autoencoder;
    }
    
    public Map<Integer, Double> detectAnomalies(List<double[]> samples) {
        Map<Integer, Double> anomalyScores = new HashMap<>();
        
        if (samples.isEmpty()) {
            return anomalyScores;
        }
        
        int inputSize = samples.get(0).length;
        
        // Build autoencoder if not already built
        if (autoencoder == null) {
            autoencoder = buildAutoencoder(inputSize);
            // In production, you would load pre-trained weights here
            // autoencoder.load(modelFile);
        }
        
        // Convert samples to INDArray
        double[][] dataArray = samples.toArray(new double[0][]);
        INDArray input = Nd4j.create(dataArray);
        
        // Get reconstructions
        INDArray reconstructed = autoencoder.output(input);
        
        // Calculate reconstruction errors
        for (int i = 0; i < samples.size(); i++) {
            INDArray originalRow = input.getRow(i);
            INDArray reconstructedRow = reconstructed.getRow(i);
            
            // Calculate mean squared error
            INDArray diff = originalRow.sub(reconstructedRow);
            double mse = diff.mul(diff).meanNumber().doubleValue();
            
            // Normalize to anomaly score
            double anomalyScore = Math.min(1.0, mse / reconstructionThreshold);
            anomalyScores.put(i, anomalyScore);
        }
        
        return anomalyScores;
    }
    
    public void trainAutoencoder(double[][] normalData, int epochs) {
        if (normalData.length == 0) {
            log.warn("No training data provided for autoencoder");
            return;
        }
        
        int inputSize = normalData[0].length;
        if (autoencoder == null) {
            autoencoder = buildAutoencoder(inputSize);
        }
        
        // Create training dataset (input = output for autoencoder)
        INDArray input = Nd4j.create(normalData);
        DataSet trainingData = new DataSet(input, input);
        
        // Train the autoencoder
        for (int epoch = 0; epoch < epochs; epoch++) {
            autoencoder.fit(trainingData);
            
            if (epoch % 10 == 0) {
                double loss = autoencoder.score();
                log.debug("Epoch {}: Loss = {}", epoch, loss);
            }
        }
        
        // Calculate reconstruction threshold based on training data
        INDArray reconstructed = autoencoder.output(input);
        double totalError = 0.0;
        
        for (int i = 0; i < normalData.length; i++) {
            INDArray originalRow = input.getRow(i);
            INDArray reconstructedRow = reconstructed.getRow(i);
            INDArray diff = originalRow.sub(reconstructedRow);
            totalError += diff.mul(diff).meanNumber().doubleValue();
        }
        
        // Set threshold as mean error + 2 standard deviations
        double meanError = totalError / normalData.length;
        reconstructionThreshold = meanError * 3.0; // Simplified threshold calculation
        
        log.info("Autoencoder training completed. Reconstruction threshold: {}", reconstructionThreshold);
    }
}