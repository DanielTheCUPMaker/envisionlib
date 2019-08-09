import dataTypes.DoubleVector;
import neuralNetworks.constants.enums.ActivationFunctionTypes;
import neuralNetworks.constants.enums.TrainingAlgorithmTypes;
import neuralNetworks.objects.NetworkPattern;
import neuralNetworks.objects.NeuralNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        NeuralNetwork network = new NeuralNetwork(ActivationFunctionTypes.SIGMOID, 2,2,1);
        List<NetworkPattern> patterns = new ArrayList<>();
        patterns.add(new NetworkPattern(Arrays.asList(1.0,0.0), Arrays.asList(1.0)));
        patterns.add(new NetworkPattern(Arrays.asList(1.0,1.0), Arrays.asList(0.0)));
        patterns.add(new NetworkPattern(Arrays.asList(0.0,1.0), Arrays.asList(1.0)));
        patterns.add(new NetworkPattern(Arrays.asList(0.0,0.0), Arrays.asList(0.0)));

        network.train(TrainingAlgorithmTypes.BACK_PROPAGATION, 0.01, patterns, 1);
        System.out.println(network.compute(new DoubleVector(Arrays.asList(1.0,0.0))));
        System.out.println(network.compute(new DoubleVector(Arrays.asList(0.0,0.0))));
        System.out.println(network.compute(new DoubleVector(Arrays.asList(0.0,1.0))));
        System.out.println(network.compute(new DoubleVector(Arrays.asList(1.0,1.0))));
    }
}
