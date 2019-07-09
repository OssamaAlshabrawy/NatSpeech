import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.evaluation.EvaluationTools;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.storage.FileStatsStorage;

import java.io.File;
import java.io.IOException;

public class UiMonitoring {

    public static void main(String[] args) throws IOException {
        StatsStorage statsStorage = new FileStatsStorage(new File("StatsTraining/EncDecLSTM_Stats_d1_07_11_2017__10_07.dl4j"));    //If file already exists: load the data from it
        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();
        uiServer.attach(statsStorage);

        /**
         * plotting Roc & PR curves from JSon
         */
//        String testSession = "d1";
//        Gson gson = new Gson();
//        String json= FileUtils.readFileToString(new File("ROCserialised/Curves_dd1v2_epoch003_11_2017__17_39"));
//        ROC roc2 = gson.fromJson(json, ROC.class);
//        EvaluationTools.exportRocChartsToHtmlFile(roc2, new File("MyPlots/256/d" + testSession + ".htm"));
    }

}
