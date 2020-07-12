import java.util.*;

public class Neuron {
    private Map<Integer, Double> synapses;
    private functionName activation;
    private double bias;
    private Double data;
    private Double error;

    //    initializes the attributes of the neuron
    public Neuron(){
        synapses = new HashMap<>();
        activation = functionName.SIGMOID;
        bias = 0;
        data = null;
        error = null;
    }

    //    adds a new connection to the neuron
    public void addSynapse(Double synapse){
        synapses.put(SynapseIndex(), synapse);
    }

    public void addSynapse(int index, Double synapse){
        synapses.put(index, synapse);
    }

    //    returns a weight value if the index is in range of the list
    public Double getSynapse(int index) {
        return synapses.getOrDefault(index, null);
    }

    //    sets a weight value of a synapse
    public void setSynapse(int index, Double synapse) {
        if(synapses.containsKey(index))
            synapses.replace(index, synapse);
    }

    private int SynapseIndex(){
        return synapses.keySet().size();
    }

    //    removes the synapse from the list
//    public void removeSynapse(int index){
//        synapses.remove(index);
//    }

    //    returns a number that matches
    public functionName getActivation() {
        return activation;
    }

    public void setActivation(functionName activation) {
        this.activation = activation;
    }

    public double getBias(){
        return bias;
    }

    public void setBias(double bias){
        this.bias = bias;
    }

    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    public Double getError() {
        return error;
    }

    public void setError(Double error) {
        this.error = error;
    }

    public int numberOfSynapses(){
        return synapses.size();
    }

    @Override
    public String toString() {
        Map<Integer, Double> connections = synapses;
        connections.put(SynapseIndex(), bias);
        return connections.toString();
    }
}
