import java.util.Arrays;

public class App {
    public static void main(String [] args) throws Exception
    {
//        determine number of neurons per layer
        int[] layerSizes = {2, 4, 1};
        double[] bounds = {-1, 1};

        NeuralNetwork network = new NeuralNetwork(layerSizes, bounds, "/*Save location*/");

        DataSets trainingData = new DataSets();

        trainingData.addDataSet(new Double[]{0.2, 0.2}, new Double[]{0.4});
        trainingData.addDataSet(new Double[]{0.3, 0.4}, new Double[]{0.7});
        trainingData.addDataSet(new Double[]{0.7, 0.1}, new Double[]{0.8});

        System.out.println(Arrays.toString(network.predict(new Double[]{0.2, 0.2})));
        System.out.println(Arrays.toString(network.predict(new Double[]{0.3, 0.4})));
        System.out.println(Arrays.toString(network.predict(new Double[]{0.3, 0.2})));

        System.out.println("Start training!");

        network.train(trainingData, 2500, .7, .008);

        try {
            System.out.println(Arrays.toString(network.predict(new Double[]{0.2, 0.2})));
            System.out.println(Arrays.toString(network.predict(new Double[]{0.3, 0.4})));
            System.out.println(Arrays.toString(network.predict(new Double[]{0.3, 0.2})));

            NeuralNetwork newNetwork = new NeuralNetwork("/*Save Location*/");

            System.out.println("RECONSTRUCTED BRAIN");
            System.out.println(Arrays.toString(newNetwork.predict(new Double[]{0.2, 0.2})));
            System.out.println(Arrays.toString(newNetwork.predict(new Double[]{0.3, 0.4})));
            System.out.println(Arrays.toString(newNetwork.predict(new Double[]{0.3, 0.2})));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}