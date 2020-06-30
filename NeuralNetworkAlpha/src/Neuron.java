import java.util.ArrayList;
import java.util.Arrays;

public class Neuron {
    private ArrayList<Double> synapses;
    private functionName activation;
    private Double data;
    private Double error;

//    initializes the attributes of the neuron
    public Neuron(){
        synapses = new ArrayList<>();
        activation = functionName.SIGMOID;
        data = null;
        error = null;
    }

//    adds a new connection to the neuron
    public void addSynapse(Double synapse){
        synapses.add(synapse);
    }

//    returns a weight value if the index is in range of the list
    public Double getSynapse(int index) {

        return (index >= 0 && index < synapses.size()) ? synapses.get(index) : null;
    }

//    sets a weight value of a synapse
    public void setSynapse(int index, Double synapse) {
        synapses.set(index, synapse);
    }

//    removes the synapse from the list
    public void removeSynapse(int index){
        synapses.remove(index);
    }

//    returns a number that matches
    public functionName getActivation() {
        return activation;
    }

    public void setActivation(functionName activation) {
        this.activation = activation;
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
        return Arrays.toString(synapses.toArray());
    }
}
