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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

@Slf4j
public class ReconstructionDetector {
    
    private MultiLayerNetwork denoiser;
    private static final double RECONSTRUCTION_THRESHOLD = 0.15;
    
    public double computeReconstructionError(double[] input) {
        try {
            if (denoiser == null) {
                denoiser = buildDenoiser(input.length);
            }
            
            // Convert input to INDArray
            INDArray inputArray = Nd4j.create(new double[][]{input});
            
            // Get denoised reconstruction
            INDArray reconstructed = denoiser.output(inputArray);
            
            // Compute reconstruction error
            INDArray diff = inputArray.sub(reconstructed);
            double mse = diff.mul(diff).meanNumber().doubleValue();
            
            // Normalize error to score
            // Higher reconstruction error indicates adversarial example
            return Math.min(1.0, mse / RECONSTRUCTION_THRESHOLD);
            
        } catch (Exception e) {
            log.error("Error in reconstruction detection", e);
            return 0.5; // Return neutral score on error
        }
    }
    
    private MultiLayerNetwork buildDenoiser(int inputSize) {
        // Build a denoising autoencoder
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12345)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(0, new DenseLayer.Builder()
                .nIn(inputSize)
                .nOut(inputSize * 2)
                .activation(Activation.RELU)
                .dropOut(0.2)
                .build())
            .layer(1, new DenseLayer.Builder()
                .nIn(inputSize * 2)
                .nOut(inputSize)
                .activation(Activation.RELU)
                .dropOut(0.2)
                .build())
            .layer(2, new DenseLayer.Builder()
                .nIn(inputSize)
                .nOut(inputSize / 2)
                .activation(Activation.RELU)
                .build())
            .layer(3, new DenseLayer.Builder()
                .nIn(inputSize / 2)
                .nOut(inputSize)
                .activation(Activation.RELU)
                .build())
            .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(inputSize)
                .nOut(inputSize)
                .activation(Activation.IDENTITY)
                .build())
            .build();
        
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        
        // In production, load pre-trained weights
        // network.load(modelFile);
        
        return network;
    }
}