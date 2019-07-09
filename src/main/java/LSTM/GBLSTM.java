package LSTM;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.CollectionInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.graph.ElementWiseVertex;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.nd4j.evaluation.classification.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.nd4j.linalg.lossfunctions.impl.LossMCXENT;
import org.nd4j.evaluation.classification.ROC;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 @author Ossama Alshabrawy
 */
public class GBLSTM {

    public static void main(String[] args) throws Exception{

        String mypath = "data/framesFiles";

        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy__HH_mm");
        Date date = new Date();

        String testSession = "d1";

        int numLinesToSkip = 0; // In case you don't have a header row..
        String delimiter = ",";

        int batchSize = 4; // # sequences (# time series)
        int nEpochs = 40;
        int rngSeed = 12345;

        double learningRate = 0.00005;

        int labelIndex = 256;
        int tbpttLength = 960; //  # time steps = 30 sec

        //Create the weights array. Note that we have 2 output classes, therefore we have 2 weights
        INDArray weightsArray = Nd4j.create(new double[]{1, 0.75});

        // Define uri's for the dataset files
        URI uri;
        Collection<URI> trainingList = new ArrayList<>();
        Collection<URI> testList     = new ArrayList<>();
        File[] allFiles = new File(mypath).listFiles();
        System.out.println("# files is: " + allFiles.length);
        for(int i=0; i< allFiles.length; i++) {
            if (allFiles[i].getName().substring(0,allFiles[i].getName().lastIndexOf('_')).equals(testSession)) {
                System.out.println("test sesion>> " + allFiles[i].toURI().toString());
                uri = new URI(allFiles[i].toURI().toString());
                testList.add(uri);
                continue;
            }
            System.out.println(allFiles[i].toURI().toString());
            uri = new URI(allFiles[i].toURI().toString());
            trainingList.add(uri);
        }

        //Load the training data:
        SequenceRecordReader trainReader = new CSVSequenceRecordReader(numLinesToSkip, delimiter);
        trainReader.initialize(new CollectionInputSplit(trainingList)); // initialised with a Collection of URIs
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainReader, batchSize, 2, labelIndex, false);
        System.out.println("train data loaded successfully..");

        //Load the test/evaluation data:
        SequenceRecordReader testReader = new CSVSequenceRecordReader(numLinesToSkip,delimiter);
        testReader.initialize(new CollectionInputSplit(testList));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, batchSize, 2, labelIndex, false);
        System.out.println("test data loaded successfully..");

        // normalise the data ..
        DataNormalization normalization = new NormalizerStandardize();
        normalization.fit(trainIter); // obtain the statistics
        trainIter.reset();
        //Use previously collected statistics to normalize on-the-fly. Each DataSet returned by 'trainData' iterator will be normalized
        trainIter.setPreProcessor(normalization); // apply the normalisation to train set Iterator
        testIter.setPreProcessor(normalization);  // apply the normalisation to test set Iterator
        System.out.println("<<<<<< Normalisation finished >>>>>>>");

        System.out.println("build the model .. ");

        Nd4j.getRandom().setSeed(rngSeed);
        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .dropOut(0.8)  // retaining
                .l2(0.0005) // l2 as a penalty on the activations
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam.Builder().learningRate(learningRate).build())

                .graphBuilder()
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTBackwardLength(tbpttLength)
                .tBPTTForwardLength(tbpttLength)

                .addInputs("inputLine")
                .setInputTypes(InputType.recurrent(256))

                .addLayer("gated_1",
                        new Bidirectional(new LSTM.Builder().nIn(256).nOut(128).activation(Activation.TANH).l2(0.0005).build()), "inputLine")
                .addLayer("gated_2",
                        new Bidirectional(new LSTM.Builder().nIn(256).nOut(128).activation(Activation.SIGMOID).l2(0.0005).build()), "inputLine")
                .addVertex("Mul_1",
                        new ElementWiseVertex(ElementWiseVertex.Op.Product), "gated_1", "gated_2")
                .addLayer("gated_1_1",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).l2(0.0005).build()), "Mul_1")
                .addLayer("gated_2_2",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.SIGMOID).l2(0.0005).build()), "Mul_1")
                .addVertex("Mul_2",
                        new ElementWiseVertex(ElementWiseVertex.Op.Product), "gated_1_1", "gated_2_2")
                .addLayer("blstm1",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).gradientNormalization(GradientNormalization.RenormalizeL2PerParamType).build()), "Mul_2")
                .addLayer("blstm2",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).gradientNormalization(GradientNormalization.RenormalizeL2PerParamType).build()), "blstm1")
                .addLayer("blstm3",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).gradientNormalization(GradientNormalization.RenormalizeL2PerParamType).build()), "blstm2")
                .addLayer("blstm4",
                        new Bidirectional(new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).gradientNormalization(GradientNormalization.RenormalizeL2PerParamType).build()), "blstm3")
                .addLayer("lstm1",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "blstm4")
                .addLayer("lstm2",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "lstm1")
                .addLayer("lstm3",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "lstm2")
                .addLayer("lstm4",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "lstm3")
                .addLayer("lstm5",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "lstm4")
                .addLayer("lstm6",
                        new LSTM.Builder().nIn(128).nOut(128).activation(Activation.TANH).dropOut(0.7).l2(0.0005).build(), "lstm5")
                .addLayer("maxout1",
                        new DenseLayer.Builder().nIn(128).nOut(128).activation(new CustomActivation()).build(), "lstm5")
                .addLayer("maxout2",
                        new DenseLayer.Builder().nIn(128).nOut(128).activation(new CustomActivation()).build(), "maxout1")
                .addLayer("maxout3",
                        new DenseLayer.Builder().nIn(128).nOut(128).activation(new CustomActivation()).build(), "maxout2")
                .addLayer("Last_Dense",
                        new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.LEAKYRELU).build(), "")
                .addLayer("_",
                        new BatchNormalization.Builder().build())
                .addLayer("output",
                        new RnnOutputLayer.Builder()
                                .nIn(128)
                                .nOut(2)
                                .activation(Activation.SOFTMAX)
                                .lossFunction(new LossMCXENT(weightsArray))   // *** Weighted loss function configured here ***
                                .build(),
                        "Last_Dense")

                .setOutputs("output")

                .build();

        ComputationGraph model = new ComputationGraph(conf);

        model.init();

        System.out.println("train the model .. ");

        StatsStorage statsStorage = new FileStatsStorage(new File("training_history_"+testSession+"_"+ df.format(date) +".dl4j"));
        model.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(1));

        for( int i=0; i<nEpochs; i++ ) {
            System.out.printf("Epoch { %d }: %n", (i + 1));
            model.fit(trainIter);
            System.out.println("Evaluate model at epoch {" + (i+1) + " }");
            Evaluation eval = model.evaluate(testIter);
            //System.out.println(eval.stats());
            System.out.println(eval.confusionToString());
            System.out.println("FNR: " + eval.falseNegativeRate());
            System.out.println("false alarm rate: " + eval.falseAlarmRate());
            System.out.println("FPR" + eval.falsePositiveRate());
            System.out.println(eval.stats());
            // plotting the ROC and PR curves:
            ROC roc = model.evaluateROC(testIter,100);
            // serialise roc to json
            Gson gson = new Gson();
            String json = gson.toJson(roc);
            FileUtils.writeStringToFile(new File("Curves_d" + testSession + "_epoch" + i + df.format(date)), json, (String) null);  // the last argument is for the encoding

            trainIter.reset();
            testIter.reset();
        }
        System.out.println("**************** Finished ********************");

    }
}

