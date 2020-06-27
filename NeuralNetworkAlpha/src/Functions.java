public class Functions {

    public final int SIGMOID_FUNCTION = 0;
    public final int RELU_FUNCTION = 1;
    public final int LEAKYRELU_FUNCTION = 2;

    public Double getValue(int function, Double x, boolean fPrime){
        switch (function){
            case SIGMOID_FUNCTION:
                return fPrime ? this.sigmoidPrime(x) : this.sigmoid(x);
            case RELU_FUNCTION:
                return fPrime ? this.reLUDerivativeFunction(x) : this.reLUFunction(x);
            case LEAKYRELU_FUNCTION:
                return fPrime ? this.leakyReLUDerivativeFunction(x) : this.leakyReLUFunction(x);
        }
        return null;
    }

    private Double sigmoid(Double x){
        return 1 / (1 + Math.exp(-x));
    }

    private Double sigmoidPrime(Double x){
        return sigmoid(x) * (1 - sigmoid(x));
    }

    private Double reLUFunction(Double x){
        return x > 0 ? x : 0;
    }

    private Double reLUDerivativeFunction(Double x){
        return x > 0 ? 1 : 0.0;
    }

    private Double leakyReLUFunction(Double x){
        return x > 0 ? x : .01 * x;
    }

    private Double leakyReLUDerivativeFunction(Double x){
        return x > 0 ? 1 : .01;
    }

    public Double getRandomDouble(Double min, Double max){
        return min + Math.random() * (max - min);
    }
}