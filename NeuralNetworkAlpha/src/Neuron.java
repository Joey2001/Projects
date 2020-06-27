import java.util.ArrayList;

public class Neuron {
    private ArrayList<Double> synapses;
    private int activation;
    private Double data;
    private Double error;

    public Neuron(){
        synapses = new ArrayList<>();
        activation = 0;
        data = null;
        error = null;
    }

    public void addSynapse(Double synapse){
        synapses.add(synapse);
    }

    public Double getSynapse(int index) {

        return (index >= 0 && index < synapses.size()) ? synapses.get(index) : null;
    }

    public void setSynapse(int index, Double synapse) {
        synapses.set(index, synapse);
    }

    public void removeSynapse(int index){
        synapses.remove(index);
    }

    public int getActivation() {
        return activation;
    }

    public void setActivation(int activation) {
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
}
