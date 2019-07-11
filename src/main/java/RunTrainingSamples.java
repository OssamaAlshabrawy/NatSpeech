import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

public class RunTrainingSamples {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {

        /** Obtain the RawDataset according to the order of the segments in the file
         * Take all the files as training and leave one out
         * the param you need is: ExcludedFile such as Adam,..etc */
//        getOrderedSeg("Tom");

        /** Obtained annotated Frames files , the param is: PartFileName or sessionName
		 * make sure the folder path in the method is original or it is for RescaledSignal
		 * // check the frames file first and see the last line, delete it if necessary..
		 */
		/**
		 * get the Hamming window frames from the annotated sessions or,
		 * get annotated frames files directly..
		 */

        int num_sessions = 15;
		for (int i=1; i<=num_sessions; i++) {
			String sessionName1 = String.valueOf(i);
			AnnotateFramesFile(sessionName1);
			System.out.println("s" + i + " finished ..");
		}
	}


	public static int countLines(String pathFile) throws IOException{
		File inFile = new File(pathFile);
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		int count=0; String line="";
		while((line=reader.readLine())!=null) {
			count =  count + 1;
		}
		reader.close();
		return count;
	}

	public static void AnnotateFramesFile(String PartFileName) throws IOException {

		GeneralParameters g = new GeneralParameters();
//        double fl= g.getWindow_len_msec()/1000.0 * g.sampleRate;

		FileWriter fw = new FileWriter(
		        new File("AllData/framesFiles/d" + PartFileName + "FramesA" + ".csv")); // A for annotated..

		ArrayList<String> Is_Speech = new ArrayList<String>();
		ArrayList<Integer> From = new ArrayList<Integer>();
		ArrayList<Integer> To = new ArrayList<Integer>();
		AnnotationFile annotation = new AnnotationFile();
		annotation.read_csv(new FileReader("AllData/annotationFiles/" + PartFileName + ".csv"), Is_Speech, From, To);
		System.out.println("Working on: " + PartFileName);

		BufferedReader br = new BufferedReader(new FileReader("AllData/annotationFiles/" + PartFileName + "Frames.csv"));
		int ind_fr=0;
		int inc = (int) ((g.getFrame_shift_msec()/1000.0)*g.getSampleRate());
		int wl = (int) ((g.getWindow_len_msec()/1000.0)*g.getSampleRate());
		String line = "";
		while((line = br.readLine()) != null) {  // iterate on the frames.. line by line .. from the csv file
			int frame_start = inc * ind_fr;
			int frame_end = frame_start + wl - 1;
			for (int j = 0; j < Is_Speech.size(); j++) {
				if (Is_Speech.get(j).contains("speech")) {
					int annot_start = (int) ((From.get(j) / 1000.0) * g.getSampleRate());
					int annot_end = (int) ((To.get(j) / 1000.0) * g.getSampleRate());
					if (frame_start >= annot_start && frame_start <= annot_end &&
							(frame_end <= annot_end || frame_end - annot_end < 0.5*wl)) {
						fw.append(line);
						fw.append(",1");
						fw.append("\n");
						break;
					}
				}
				else {
                    if (Is_Speech.get(j).contains("non_speech")) {
                        int annot_start = (int) ((From.get(j) / 1000.0) * g.getSampleRate());
                        int annot_end = (int) ((To.get(j) / 1000.0) * g.getSampleRate());
                        if (frame_start >= annot_start && frame_start <= annot_end &&
                                (frame_end <= annot_end || frame_end - annot_end < 0.5 * wl)) {
                            fw.append(line);
                            fw.append(",0");
                            fw.append("\n");
                            break;
                        }
                    }
                }
			}
			ind_fr++;
		}
		// don't forget to close the reader and writer...
		br.close();

        fw.flush();
        fw.close();
	}

}
