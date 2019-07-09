import java.io.*;
import java.util.ArrayList;

public class AnnotationFile {
	
	/**
	 * CSVFile csv=new CSVFileRead();
	 * csv.read_csv(new FileReader("path"));
	 */
	
	public void read_csv(FileReader file, ArrayList<String> Is_Speech,
			ArrayList<Integer> From, ArrayList<Integer> To) throws IOException {
		BufferedReader br;
		String line;

			br = new BufferedReader(file);
			line = br.readLine(); // skip the header row..
			while ((line = br.readLine()) != null) {
				// use comma as a separator
				String[] col_data = line.split(",");
				Is_Speech.add(col_data[0]);
				From.add(Integer.parseInt(col_data[1]));
				To.add(Integer.parseInt(col_data[2]));
				}
			br.close();
			}

	public ArrayList<Byte> getIs_Speech_1_0(ArrayList<String> Is_Speech) {
		ArrayList<Byte> Is_Speech10 = new ArrayList<Byte>();
		// Replace Is_Speech to (0:non-speech, 1:speech)
        for (int i=0; i<Is_Speech.size(); i++) {
        	if (Is_Speech.get(i).contains("non_speech")) { Is_Speech.set(i, "0"); }
        	if (Is_Speech.get(i).contains("S1")) { Is_Speech.set(i, "1"); }
        	}
        for (String s : Is_Speech) {
			Is_Speech10.add(Byte.parseByte(s));
			}
        return Is_Speech10;
        }

	public void AnnotateByFrame(FileReader testFile, FileReader testFramesFile, File GroundTruthFileName)
			throws FileNotFoundException, IOException{
		ArrayList<String> Is_Speech = new ArrayList<String>();
		ArrayList<Integer> From = new ArrayList<Integer>();
		ArrayList<Integer> To = new ArrayList<Integer>();
		read_csv(testFile, Is_Speech, From, To);
		// obtain number of frames
		BufferedReader br = new BufferedReader(testFramesFile);
		int nf = 0;
        while(br.readLine() != null) { nf = nf + 1; } // number of frames
        br.close();
        GeneralParameters g = new GeneralParameters();
        // write to GroundTruthFrames.csv
        FileWriter writer = new FileWriter(GroundTruthFileName);
        // Iterate on the frames..
        for(int i=0; i<nf; i++) {
        	float start = g.frame_shift_msec * i;
        	float end = start + g.window_len_msec;
        	for(int j=0; j<From.size(); j++) { // iterate on annotations
        		if(start>=From.get(j) && start<To.get(j) && end<=To.get(j)) {
        			if(Is_Speech.get(j).contains("S1")==true) {
        				writer.append(Integer.toString(i+1));
            			writer.append(",");
        				writer.append("1");
        				writer.append("\n");
        				}
        			else if (Is_Speech.get(j).contains("non_speech")==true) {
        				writer.append(Integer.toString(i+1));
            			writer.append(",");
        				writer.append("0");
        				writer.append("\n");
        				}
        		}
        			else if(start>=From.get(j) && start<To.get(j) && end-To.get(j)<0.5*g.window_len_msec) {
            			if(Is_Speech.get(j).contains("S1")==true) {
            				writer.append(Integer.toString(i+1));
                			writer.append(",");
            				writer.append("1");
            				writer.append("\n");
            				}
            			else if (Is_Speech.get(j).contains("non_speech")==true) {
            				writer.append(Integer.toString(i+1));
                			writer.append(",");
            				writer.append("0");
            				writer.append("\n");
            				}
        			}
        	}
        }
        writer.flush();
        writer.close();
	}

	public void RemoveLineFromFile(File file, String line2remove) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		File tempFile = new File(file.getAbsolutePath() + ".tmp");
		PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
		String line = "";
		while((line=br.readLine()) != null) {
			if(!line.contains(line2remove)) {
				pw.println(line);
				}
			}
		br.close();
		pw.flush();
		pw.close();
		// delete the original file
		if(!file.delete()) {
			System.out.println("File could not be deleted..");
		}
		// rename the temp file to be the original file
		if(!tempFile.renameTo(file)) {
			System.out.println("Could not rename file..");
		}
		System.out.println("Done...");
		}
	
}
