import java.io.*;

/**
 * Created by ossama Alshabrawy.
 */
public class SequenceUtilities {

    public void split(String FilePath, long splitlen) {
        long leninfile = 0, leng = 0;
        int count = 1;
        String line;
        try {
            File filename = new File(FilePath);
            BufferedReader infile = new BufferedReader(new FileReader(filename));
            line = infile.readLine();
            while (line != null) {
                filename = new File(FilePath.substring(0, FilePath.lastIndexOf('d')) + "/split/" +
                        FilePath.substring(FilePath.lastIndexOf('d'),FilePath.lastIndexOf('F'))+ "_" + count + "FramesA.csv");
                BufferedWriter outfile = new BufferedWriter(new FileWriter(filename));
                while (line != null && leng < splitlen) {
                    outfile.write(line);
                    outfile.write("\n");
                    leng++;
                    line = infile.readLine();
                }
                leninfile += leng;
                leng = 0;
                outfile.close();
                count++;
            }
            infile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        SequenceUtilities seq = new SequenceUtilities();
        seq.split("data/framesFiles/d15FramesA.csv", 19200);

    }
}
