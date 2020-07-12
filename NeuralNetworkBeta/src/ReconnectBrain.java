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
            ArrayList<Neuron[]> buildBrain = reformat(rawData);

            return new NeuralNetwork(buildBrain);
        }
        return null;
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
                int trim = preProcess.get(layer)[neuron].length() - 1;
                preProcess.get(layer)[neuron] = preProcess.get(layer)[neuron].substring(1, trim).replaceAll("\\s+", "");
                String[] synapses = preProcess.get(layer)[neuron].split(",");
                neurons[neuron] = new Neuron();
                if(layer == layerSizes.length - 1)
                    neurons[neuron].setActivation(functionName.LEAKY_RECT_LIN);

                for (int synapse = 0; synapse < synapses.length; synapse++) {
                    String[] connection = synapses[synapse].split("=");
                    if (!synapses[synapse].contains("null")) {
                        int location = Integer.parseInt(connection[0]);
                        double weight = Double.parseDouble(connection[1]);
                        if (synapse < synapses.length - 1)
                            neurons[neuron].addSynapse(location, weight);
                        else
                            neurons[neuron].setBias(weight);
                    }
                }
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
            String[] part1 = s.toString().split("L");
            ArrayList<String[]> part2 = new ArrayList<>();
            for (String string : part1)
                part2.add(string.split("N"));
            return part2;
        }catch(FileNotFoundException ignored){

        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}