import java.io.*;
import java.util.*;

public class NeuralNetwork {

    private ArrayList<Neuron[]> brain;
    private double[] networkBias;
    private Functions functions = new Functions();
    private String saveDestination;

//    Initializes the neural network and takes in an integer and double array
//    the int array is used to determine the network size and the double array is used to
//    determine search bounds
    public NeuralNetwork(int[] allLayers, double[] searchBounds, String saveDestination){
        brain = new ArrayList<>();
        networkBias = new double[allLayers.length];
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

    public NeuralNetwork(ArrayList<Neuron[]> brain, double[] networkBias){
        this.brain = brain;
        this.networkBias = networkBias;
    }

    public NeuralNetwork(String brainLocation){
        ReconnectBrain reconnectBrain = new ReconnectBrain(brainLocation);
        NeuralNetwork newBrain = reconnectBrain.reconstruct();
        this.brain = newBrain.getBrain();
        this.networkBias = newBrain.getBias();
    }

//    Initializing the weights with random values
    public void Connect(Double min, Double max){
        for(int layer = 0; layer < brain.size() - 1; layer++){
            networkBias[layer] = functions.getRandomDouble(min, max);
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++) {
                for (int nextNeuron = 0; nextNeuron < brain.get(layer + 1).length; nextNeuron++)
                    brain.get(layer)[neuron].setSynapse(nextNeuron, functions.getRandomDouble(min, max));
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
                Double newData = networkBias[layer - 1];

                for(int prevNeuron = 0; prevNeuron < brain.get(layer - 1).length; prevNeuron++)
                    newData += brain.get(layer - 1)[prevNeuron].getSynapse(neuron) * brain.get(layer - 1)[prevNeuron].getData();

                newData = functions.getValue(brain.get(layer)[neuron].getActivation(), newData, false);
                brain.get(layer)[neuron].setData(newData);

                if(layer == brain.size() - 1)
                    output[neuron] = brain.get(layer)[neuron].getData();
            }
        }
        return output;
    }

//    findError changes the error values on the neurons
    private void findError(Double[] target){
        for(int outNeuron = 0; outNeuron < brain.get(brain.size() - 1).length; outNeuron++){
            Double error = calcError(target[outNeuron], brain.size() - 1, outNeuron, false);
            brain.get(brain.size() - 1)[outNeuron].setError(error);
        }

        for(int layer = brain.size() - 2; layer >= 0; layer--){
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++){
                double sum = 0.0;

                for(int nextNeuron = 0; nextNeuron < brain.get(layer + 1).length; nextNeuron++)
                    sum += brain.get(layer + 1)[nextNeuron].getError() * brain.get(layer)[neuron].getSynapse(nextNeuron);

                brain.get(layer)[neuron].setError(calcError(sum, layer, neuron, true));
            }
        }
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
                    brain.get(layer - 1)[prevNeuron].setSynapse(neuron, brain.get(layer - 1)[prevNeuron].getSynapse(neuron) + delta);
                }
            }
        }
    }

//    pruneNetwork has not been implemented but will delete synapses that have a small weight
//    training after pruning is necessary considering the structure of the network is changed
    public void pruneNetwork(double threshold){
        for (Neuron[] neurons : brain) {
            for (Neuron neuron : neurons) {
                int synapses = neuron.numberOfSynapses() - 1;
                for (int synapse = synapses; synapse >= 0; synapse--) {
                    if (Math.abs(neuron.getSynapse(synapse)) <= threshold)
                        neuron.removeSynapse(synapse);
                }
            }
        }
    }

//    trainNeuralNetwork is the method that handles predict, findError, and changeWeights
//    and passes values correctly
    public void trainNeuralNetwork(Double[] input, Double[] target, double learningRate) throws Exception {
        predict(input);
        findError(target);
        changeWeights(learningRate);
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

    private double[] getBias(){
        return networkBias;
    }

    public void saveBrain(){

        StringBuilder s = new StringBuilder();
        s.append(Arrays.toString(networkBias)).append("B");
        for (Neuron[] neurons : this.brain) {
            for (Neuron neuron : neurons) {
                s.append(neuron.toString()).append("N");
            }
            s.append("L");
        }

        String neuralText = s.toString();

        try(FileWriter fileWriter = new FileWriter(saveDestination)){
            fileWriter.write(neuralText);
            fileWriter.close();
        } catch(IOException ignored){

        }
    }

    public ArrayList<String[]> pullFromText(String finalDestination) {
        try(FileReader fileReader = new FileReader(finalDestination)){
            int i;
            StringBuilder s = new StringBuilder();
            while((i = fileReader.read()) != -1){
                s.append((char) i);
            }
            String part1 = s.toString().substring(0, s.toString().indexOf("B"));
            String part2 = s.toString().substring(s.toString().indexOf("B") + 1);
            String[] part3 = part2.split("L");
            ArrayList<String[]> part4 = new ArrayList<>();
            part4.add(part1.split("B"));
            for (String string : part3)
                part4.add(string.split("N"));
            return part4;
        }catch(FileNotFoundException ignored){

        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}