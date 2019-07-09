import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Created by Ossama Alshabrawy on 17/08/16.
 */
public class CSVutilities {

    public int[] myFileDimensions(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String[] lineTokens = br.readLine().split(",");
        int dim = lineTokens.length;
        int count = 1; // 'cause we already have read one line
        while(br.readLine()!=null) {
            count++;
        }
        return new int[]{count,dim};
    }

    public void replaceVals(String CSVfilePath, String ouputFilePath,
                            String find_this, String replace_with) throws IOException{
        Path path = Paths.get(CSVfilePath);
        Path outpath = Paths.get(ouputFilePath);
        Charset charset = StandardCharsets.US_ASCII;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll(find_this, replace_with);
        Files.write(outpath, content.getBytes(charset));
    }
}
