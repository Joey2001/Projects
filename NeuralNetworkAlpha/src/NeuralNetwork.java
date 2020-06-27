import java.util.ArrayList;
import java.util.Arrays;

public class NeuralNetwork {
    private ArrayList<Neuron[]> brain;
    private double[] networkBias;
    private Functions functions = new Functions();

    public NeuralNetwork(int[] allLayers, double[] searchBounds){
        brain = new ArrayList<>();
        networkBias = new double[allLayers.length];

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
            brain.get(brain.size() - 1)[lastNeurons].setActivation(functions.LEAKYRELU_FUNCTION);

        Connect(searchBounds[0], searchBounds[1]);
    }

    public void Connect(Double min, Double max){
        for(int layer = 0; layer < brain.size() - 1; layer++){
            networkBias[layer] = functions.getRandomDouble(min, max);
            for(int neuron = 0; neuron < brain.get(layer).length; neuron++) {
                for (int nextNeuron = 0; nextNeuron < brain.get(layer + 1).length; nextNeuron++)
                    brain.get(layer)[neuron].setSynapse(nextNeuron, functions.getRandomDouble(min, max));
            }
        }
    }

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

    private Double calcError(Double target, int layer, int neuron, boolean sum){
        int activation = brain.get(layer)[neuron].getActivation();
        Double weight = sum ? target : brain.get(layer)[neuron].getData() - target;
        return functions.getValue(activation, brain.get(layer)[neuron].getData(), true) * weight;
    }

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

    public void pruneNetwork(double threshold){
        ArrayList<Double> connections = new ArrayList<>();
        for (Neuron[] neurons : brain) {
            for (Neuron neuron : neurons) {
                int synapses = neuron.numberOfSynapses() - 1;
                for (int synapse = synapses; synapse >= 0; synapse--)
                    connections.add(neuron.getSynapse(synapse));
//                    if (Math.abs(neuron.getSynapse(synapse)) <= threshold) {
//                        neuron.removeSynapse(synapse);
//                        connections.add(neuron.getSynapse(synapse));
//                    }
            }
        }
        System.out.println(Arrays.toString(connections.toArray()));
    }

    public void trainNeuralNetwork(Double[] input, Double[] target, double learningRate) throws Exception {
        predict(input);
        findError(target);
        changeWeights(learningRate);
    }
}