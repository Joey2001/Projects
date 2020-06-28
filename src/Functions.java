enum functionName{
    SIGMOID, RECT_LIN, LEAKY_RECT_LIN
}

public class Functions {

//    returns a value of a function based on which function is passed in
    public Double getValue(functionName function, Double x, boolean derivative){
        switch (function){
            case SIGMOID:
                return derivative ? this.sigmoidPrime(x) : this.sigmoid(x);
            case RECT_LIN:
                return derivative ? (x > 0 ? 1 : 0.0) : (x > 0 ? x : 0);
            case LEAKY_RECT_LIN:
                return derivative ? (x > 0 ? 1 : .01) : (x > 0 ? x : .01 * x);
        }
        return null;
    }

    private Double sigmoid(Double x){
        return 1 / (1 + Math.exp(-x));
    }

    private Double sigmoidPrime(Double x){
        return sigmoid(x) * (1 - sigmoid(x));
    }

//    returns a random value between the min and max values
    public Double getRandomDouble(Double min, Double max){
        return min + Math.random() * (max - min);
    }
}