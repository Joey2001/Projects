import java.io.*;
import java.util.*;

public class NeuralNetwork {

    private ArrayList<Neuron[]> brain;
    private ArrayList<Double> deltaError;
    private Functions functions = new Functions();
    private String saveDestination;

    //    Initializes the neural network and takes in an integer and double array
//    the int array is used to determine the network size and the double array is used to
//    determine search bounds
    public NeuralNetwork(int[] allLayers, double[] searchBounds, String saveDestination){
        brain = new ArrayList<>();
        deltaError = new ArrayList<>();
        this.saveDestination = saveDestination;

        for(int layer = 0; layer < allLayers.length; layer++){
            Neuron[] aLayer = new Neuron[allLayers[layer]];
            for(int neuron = 0; neuron < aLayer.length; neuron++)
                aLayer[neuron] = new Neuron();

            brain.add(aLayer);
            if(layer > 0) {
                for (int neuron = 0; neuron < brain.get(layer - 1).length; neuron++)
                    for (int nextNeuron = 0; nextNeuron < brain.get(layer).length; nextNeuron++)
                        brain.get(layer - 1)[neuron].addSynapse(null);
            }
        }
        for(int lastNeurons = 0; lastNeurons < brain.get(brain.size() - 1).length; lastNeurons++)
            brain.get(brain.size() - 1)[lastNeurons].setActivation(functionName.LEAKY_RECT_LIN);

        Connect(searchBounds[0], searchBounds[1]);
    }

    public NeuralNetwork(ArrayList<Neuron[]> brain){
        this.brain = brain;
    }

    public NeuralNetwork(String brainLocation){
        NeuralNetwork newBrain = new ReconnectBrain(brainLocation).reconstruct();
        this.brain = newBrain.getBrain();
    }

    //    Initializing the weights with random values
    public void Connect(Double min, Double max){
        for(int layer = 0; layer < brain.size() - 1; layer++){
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++) {
                for (int nextNeuron = 0; nextNeuron < brain.get(layer + 1).length; nextNeuron++)
                    brain.get(layer)[neuron].setSynapse(nextNeuron, functions.getRandomDouble(min, max));
                brain.get(layer)[neuron].setBias(functions.getRandomDouble(min, max));
            }
        }
    }

    //    Predict predicts what the output should be given what is passed in
    public Double[] predict(Double[] input) throws Exception {
        if(input.length != brain.get(0).length)
            throw new Exception("Data does not match input size");

        for(int inputNeuron = 0; inputNeuron < brain.get(0).length; inputNeuron++)
            brain.get(0)[inputNeuron].setData(input[inputNeuron]);

        Double[] output = new Double[brain.get(brain.size() - 1).length];

        for(int layer = 1; layer < brain.size(); layer++){
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++){
                Double newData = brain.get(layer)[neuron].getBias();

                for(int prevNeuron = 0; prevNeuron < brain.get(layer - 1).length; prevNeuron++) {
                    Double connection = brain.get(layer - 1)[prevNeuron].getSynapse(neuron);
                    if (connection != null)
                        newData += connection * brain.get(layer - 1)[prevNeuron].getData();
                }

                newData = functions.getValue(brain.get(layer)[neuron].getActivation(), newData, false);
                brain.get(layer)[neuron].setData(newData);

                if(layer == brain.size() - 1)
                    output[neuron] = brain.get(layer)[neuron].getData();
            }
        }
        return output;
    }

    //    findError changes the error values on the neurons
    private Double[] findError(Double[] target){
        Double[] error = new Double[brain.get(brain.size() - 1).length];
        for(int outNeuron = 0; outNeuron < error.length; outNeuron++){
            error[outNeuron] = calcError(target[outNeuron], brain.size() - 1, outNeuron, false);
            brain.get(brain.size() - 1)[outNeuron].setError(error[outNeuron]);
        }

        for(int layer = brain.size() - 2; layer >= 0; layer--){
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++){
                double sum = 0.0;

                for(int nextNeuron = 0; nextNeuron < brain.get(layer + 1).length; nextNeuron++) {
                    Double connection = brain.get(layer)[neuron].getSynapse(nextNeuron);
                    if(connection != null)
                        sum += brain.get(layer + 1)[nextNeuron].getError() * connection;
                }

                brain.get(layer)[neuron].setError(calcError(sum, layer, neuron, true));
            }
        }
        return error;
    }

    //    calcError calculates what the error should be
    private Double calcError(Double target, int layer, int neuron, boolean sum){
        functionName activation = brain.get(layer)[neuron].getActivation();
        Double weight = sum ? target : brain.get(layer)[neuron].getData() - target;
        return functions.getValue(activation, brain.get(layer)[neuron].getData(), true) * weight;
    }

    //    changeWeights changes the value of the connections based on the learning rate, data in the
//    neuron, and the error of the neuron
    private void changeWeights(double learningRate){
        for(int layer = 1; layer < brain.size(); layer++){
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++){
                for(int prevNeuron = 0; prevNeuron < brain.get(layer - 1).length; prevNeuron++){
                    Double delta = -learningRate * brain.get(layer - 1)[prevNeuron].getData() * brain.get(layer)[neuron].getError();
                    Double connection = brain.get(layer - 1)[prevNeuron].getSynapse(neuron);
                    if(connection != null)
                        brain.get(layer - 1)[prevNeuron].setSynapse(neuron, connection + delta);
                }
            }
        }
    }

    //    pruneNetwork has not been implemented but will delete synapses that have a small weight
//    training after pruning is necessary considering the structure of the network is changed
    public void pruneNetwork(double threshold){
        for (Neuron[] neurons : brain) {
            for (Neuron neuron : neurons) {
                for (int synapse = neuron.numberOfSynapses() - 1; synapse >= 0; synapse--) {
                    if (Math.abs(neuron.getSynapse(synapse)) <= threshold)
                        neuron.setSynapse(synapse, null);
                }
            }
        }
    }

    //    trainNeuralNetwork is the method that handles predict, findError, and changeWeights
//    and passes values correctly
    private void trainNeuralNetwork(Double[] input, Double[] target, double learningRate, int iteration, int totalIteration, boolean limitLearning) throws Exception {
        predict(input);

        Double[] findError = findError(target);
        double error = 0;
        for(double errors : findError)
            error += errors;
        deltaError.add(error);
        if(deltaError.size() > 3) deltaError.remove(0);

        boolean fixedStep = limitLearning && .8 * iteration == totalIteration;

        double adaptiveLearningRate = (fixedStep || deltaError.size() <= 2) ? learningRate : AdaptiveLearningRate(deltaError);

        changeWeights(adaptiveLearningRate);
    }

    public void train(DataSets trainingData, int iterations, double pruningThreshold, double defaultLearningRate) throws Exception {
        for(int iteration = 0; iteration < iterations/2; iteration++){
            for (int j = 0; j < trainingData.size(); j++) {
                trainNeuralNetwork(trainingData.getDataSet(j)[0], trainingData.getDataSet(j)[1], defaultLearningRate, iteration, iterations, false);
            }
        }

        pruneNetwork(pruningThreshold);

        for(int iteration = 0; iteration < iterations/2; iteration++){
            for (int j = 0; j < trainingData.size(); j++) {
                trainNeuralNetwork(trainingData.getDataSet(j)[0], trainingData.getDataSet(j)[1], defaultLearningRate, iteration, iterations, true);
            }
        }

        saveBrain();
    }

    private double AdaptiveLearningRate(ArrayList<Double> deltaError) {
        double x = (-(deltaError.get(2)) + 4*(deltaError.get(1)) - 3*(deltaError.get(0)))/2;
        return (Math.exp(-(x * x)/2))/(2 * Math.sqrt(Math.PI));
    }

    //    Used for diagnosing the network, helpful and gives a little more information
    public void printNetwork(){
        for(Neuron[] neurons : brain){
            for(Neuron neuron : neurons){
                System.out.println(neuron.toString());
            }
            System.out.println("LAYER CHANGE");
        }
    }

    private ArrayList<Neuron[]> getBrain(){
        return brain;
    }

    private void saveBrain(){

        StringBuilder s = new StringBuilder();
        for (Neuron[] neurons : this.brain) {
            for (Neuron neuron : neurons) {
                s.append(neuron.toString()).append("N");
            }
            s.append("L");
        }

        String neuralText = s.toString();

        try(FileWriter fileWriter = new FileWriter(saveDestination)){
            fileWriter.write(neuralText);
        } catch(IOException ignored){

        }
    }
}