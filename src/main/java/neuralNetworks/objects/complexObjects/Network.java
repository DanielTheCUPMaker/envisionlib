package neuralNetworks.objects.complexObjects;

import dataTypes.Data;
import neuralNetworks.algorithmics.ActivationFunction;
import neuralNetworks.algorithmics.TrainingAlgorithm;
import neuralNetworks.constants.enums.ActivationFunctionTypes;
import neuralNetworks.constants.enums.TrainingAlgorithmTypes;
import neuralNetworks.objects.exception.NoCorrespondingWeightsException;
import neuralNetworks.objects.basicObjects.Neuron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Network {

    private final List<Layer> layers;
    private final List<WeightsMat> weightMatrices;
    private final List<BiasWeightPair> biasesAndWeights;

    private final List<DataCluster> dataClusters;
    private final ActivationFunction activationFunction;
    private final TrainingAlgorithm trainingAlgorithm;

    public Network(List<Data> dataList, int clusterSize, ActivationFunctionTypes functionType, TrainingAlgorithmTypes algorithmType, double learningRate, double acceptedError, Integer... layerSizes) {//in the future change Data to List<Data> and get TrainingAlgorithm or Enum of it
        dataClusters = new ArrayList<>();
        Collections.shuffle(dataList);
        divideDataIntoClusters(dataList, clusterSize);
        activationFunction = new ActivationFunction(functionType);
        trainingAlgorithm = algorithmType.getAlgorithm(learningRate, acceptedError);

        layers = initLayers(Arrays.asList(layerSizes));
        weightMatrices = initWeightMatrices();
        biasesAndWeights = initBiasesAndWeights();
    }

    private void divideDataIntoClusters(List<Data> dataList, int clusterSize) {
        while (!dataList.isEmpty()) {
            DataCluster cluster = new DataCluster(clusterSize);
            dataList.removeAll(cluster.addData(dataList));
            dataClusters.add(cluster);
        }
    }

    private List<Layer> initLayers(List<Integer> layerSizes) {
        return layerSizes.stream()
                .map(Layer::new)
                .collect(Collectors.toList());
    }

    private List<WeightsMat> initWeightMatrices() {
        return IntStream.range(0, layers.size())
                .skip(1)
                .mapToObj(e -> new WeightsMat(layers.get(e-1).size(), layers.get(e).size()))
                .collect(Collectors.toList());
    }

    private List<BiasWeightPair> initBiasesAndWeights() {
        return layers.stream()
                .skip(1)
                .map(l -> new BiasWeightPair(l.size()))
                .collect(Collectors.toList());
    }

    public void train() {
        dataClusters.forEach(c -> addPatterns(c));
    }

//    private void addPatterns(DataCluster cluster) {
//        cluster.stream()
//                .forEach(d -> replaceWeights(addUpWeightMats(weightMatrices, addPattern(d))));
//    }
//
//    private List<WeightsMat> addUpWeightMats(List<WeightsMat> a, List<WeightsMat> b) {
//        return IntStream.range(0, a.size())
//                .mapToObj(m -> IntStream.range(0, a.get(m).size())
//                        .mapToObj(v -> a.get(m).get(v).sum(b.get(m).get(v)))
//                        .collect(Collectors.toCollection(WeightsMat::new)))
//                .collect(Collectors.toList());
//    }

    private void addPatterns(DataCluster cluster) {
        do {
            cluster.forEach(this::learnPattern);
        } while (!hasLearnedCluster(cluster));
    }

    private boolean hasLearnedCluster(DataCluster cluster) {
        return cluster.stream()
                .allMatch(d -> {
                    System.out.printf("%.5f ", layers.get(layers.size()-1).get(0).get());
                    System.out.printf(d.getOutputPointsAsNeurons().toString() + "\n");
                    return trainingAlgorithm.hasLearned(layers.get(layers.size()-1), d);
                });
    }

    private void learnPattern(Data outputPattern) {
        feedForward(outputPattern.getInputPointsAsNeurons());
            replaceWeights(trainingAlgorithm.computeOutputPattern(layers, weightMatrices, outputPattern));
            feedForward(outputPattern.getInputPointsAsNeurons());
            //System.out.printf("%.5f\n", layers.get(layers.size()-1).get(0).get());
    }

    public List<Double> compute(Data d) {
        feedForward(d.getInputPointsAsNeurons());
        return layers.get(layers.size()-1).stream()
                .map(Neuron::new)
                .mapToDouble(n -> n.get())
                .collect(ArrayList::new,ArrayList::add,ArrayList::addAll);
    }

    private void replaceWeights(List<WeightsMat> newWeights) {
        weightMatrices.clear();
        weightMatrices.addAll(newWeights);
    }

    private void feedForward(Layer input) {
        updateInputNeurons(input);
        IntStream.range(0, layers.size())
                .skip(1)
                .forEach(i -> feedNextLayer(layers.get(i-1), layers.get(i), biasesAndWeights.get(i-1)));
    }

    private void updateInputNeurons(Layer input) {
        layers.get(0).updateLayer(input);
    }

    private void feedNextLayer(Layer prevLayer, Layer nextLayer, BiasWeightPair biasWeights) {
        WeightsMat WeightsMat = getCorrespondingWeights(nextLayer);
        WeightVector biasAdditions = biasWeights.getAdditionsToNextLayer();

        nextLayer.updateLayer(calcNextValues(WeightsMat, prevLayer, biasAdditions));
    }

    private WeightsMat getCorrespondingWeights(Layer layer){
        try {
            checkIfLayerHasCorrespondingWeights(layer);
        } catch (NoCorrespondingWeightsException e) {
            e.printStackTrace();
        }
        return weightMatrices.get(layers.indexOf(layer)-1);
    }

    private void checkIfLayerHasCorrespondingWeights(Layer layer) throws NoCorrespondingWeightsException {
        if(layers.indexOf(layer) <= 0)
            throw new NoCorrespondingWeightsException();
    }

    private Layer calcNextValues(WeightsMat W, Layer a, WeightVector biasAdditions) {
        return biasAdditions.sum(W.mulByNeurons(a)).stream()
                .map(activationFunction::process)
                .map(Neuron::new)
                .collect(Collectors.toCollection(Layer::new));
    }
}
