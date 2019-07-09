import java.io.*;

import javax.sound.sampled.UnsupportedAudioFileException;

public class RunFrames {

	public static void main(String[] args) throws IOException {
        String pathIn = "data";
        String pathOut = "data/framesFiles/";
	    writeframesToCSV(pathIn,pathOut);

	}

	public static void writeframesToCSV(String pathIn,String pathOut) {
        GeneralParameters g = new GeneralParameters();
        try{
            AudioSampleReader sr = null;
            FileWriter writer = null;
            File[] files = new File(pathIn).listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isFile() && files[i].isHidden() == false && files[i].getName().contains(".wav")) {
                    sr = new AudioSampleReader(new File(pathIn + "/" + files[i].getName() ) );
                    double[] samples = new double[(int)sr.getSampleCount()];
                    sr.getInterleavedSamples(0, sr.getSampleCount(), samples);  // get the raw samples from the .wav audio file
                    Framing enframe = new Framing(samples, g.getWindow_len_msec(), g.getFrame_shift_msec(), g.getSampleRate());
                    double[][] frames = enframe.getframes();
                    // writing the frames to .csv file
                    writer = new FileWriter(pathOut +
                            files[i].getName().substring(0, files[i].getName().lastIndexOf(".")) + "Frames.csv");
                    for (int row = 0; row < frames.length; row++)
                    {
                        for (int col = 0; col < frames[row].length; col++) {
                            writer.append(Double.toString(frames[row][col]));
                            if(col < frames[row].length - 1) { writer.append(','); }
                        }
                        writer.append("\n");
                    }
                    System.out.println("The frames file for " + files[i].getName() + " is ready ...");
                }
            }

            writer.flush();
            writer.close();

        }

        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



}
