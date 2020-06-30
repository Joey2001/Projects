import java.io.*;
import java.util.ArrayList;

public class ReconnectBrain {

    private String location;

    public ReconnectBrain(String location){
        this.location = location;
    }

    public NeuralNetwork reconstruct(){
        ArrayList<String[]> rawData = pullFromText(location);
        if(rawData != null) {
            double[] networkBias = retrieveBias(rawData.get(0));
            rawData.remove(0);
            ArrayList<Neuron[]> buildBrain = reformat(rawData);

            return new NeuralNetwork(buildBrain, networkBias);
        }
        return null;
    }

    private double[] retrieveBias(String[] strings) {
        strings = strings[0].substring(1, strings[0].length() - 1).split(",");
        double[] bias = new double[strings.length];
        for(int i = 0; i < bias.length; i++)
            bias[i] = Double.parseDouble(strings[i]);
        return bias;
    }

    private ArrayList<Neuron[]> reformat(ArrayList<String[]> preProcess){
        ArrayList<Neuron[]> reconstruction = new ArrayList<>();

        int[] layerSizes = new int[preProcess.size()];
        for(int layer = 0; layer < layerSizes.length; layer++){
            layerSizes[layer] = preProcess.get(layer).length;
        }

        for(int layer = 0; layer < layerSizes.length; layer++){
            Neuron[] neurons = new Neuron[layerSizes[layer]];
            for(int neuron = 0; neuron < neurons.length; neuron++){
                preProcess.get(layer)[neuron] = preProcess.get(layer)[neuron].substring(1, preProcess.get(layer)[neuron].length() - 1);
                String[] synapses = preProcess.get(layer)[neuron].split(",");
                neurons[neuron] = new Neuron();
                if(layer == layerSizes.length - 1)
                    neurons[neuron].setActivation(functionName.LEAKY_RECT_LIN);

                for (String synapse : synapses)
                    if (!synapse.isEmpty())
                        neurons[neuron].addSynapse(Double.parseDouble(synapse));
            }
            reconstruction.add(neurons);
        }

        return reconstruction;
    }

    private ArrayList<String[]> pullFromText(String finalDestination) {
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