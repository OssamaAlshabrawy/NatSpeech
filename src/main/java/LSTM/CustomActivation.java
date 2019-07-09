package LSTM;

import org.nd4j.linalg.activations.BaseActivationFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.scalar.ScalarMax;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;


public class CustomActivation extends BaseActivationFunction{


    @Override
    public INDArray getActivation(INDArray in, boolean training) {
        Nd4j.getExecutioner().execAndReturn(new ScalarMax(in, 1));
        return in;
    }


    @Override
    public Pair<INDArray,INDArray> backprop(INDArray in, INDArray epsilon) {
        assertShape(in, epsilon);
        return new Pair<>(epsilon, null);
    }

}