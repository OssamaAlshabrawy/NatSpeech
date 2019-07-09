import java.io.*;
import javax.sound.sampled.*;

/**
 * This class is to read the interleaved samples from an audio file
 * USAGE:
 * audioSampleReader.getInterleavedSamples(long begin, long end, double[] samples)
 *
 */

public class AudioSampleReader {
	private AudioInputStream audioInputStream;
    private AudioFormat format;
    
    public AudioSampleReader(File file)
            throws UnsupportedAudioFileException, IOException {
        audioInputStream = AudioSystem.getAudioInputStream(file);
        format = audioInputStream.getFormat();
    }
    
    // Return audio format, and through it, most properties of
    // the audio file: sample size, sample rate, etc.
    public void getFormatData() {
    	System.out.println("nbChannel=" + format.getChannels());
        System.out.println("frameRate=" + format.getFrameRate());
        System.out.println("sampleSize=" + format.getSampleSizeInBits());
        System.out.println("nbSamples=" + getSampleCount());
    }
    // Return Sample Rate...
    public double getSampleRate() {
    	return format.getFrameRate();
    }
    // Return the number of samples of all channels
    public long getSampleCount() {
        long total = (audioInputStream.getFrameLength() *
            format.getFrameSize() * 8) / format.getSampleSizeInBits();
        return total / format.getChannels();
    }
    
    // Get the intervealed decoded samples for all channels, from sample
    // index begin (included) to sample index end (excluded) and copy
    // them into samples. end must not exceed getSampleCount(), and the
    // number of samples must not be so large that the associated byte
    // array cannot be allocated
    public void getInterleavedSamples(long begin, long end,
            double[] samples) throws IOException,
                IllegalArgumentException {
        long nbSamples = end - begin; // coz it starts at begin and finishes at (end-1)
        // nbBytes = SampleRate * FrameSize, or
        // nbBytes = nbSamples * sampleSizeinByte * nbChannels
        long nbBytes = nbSamples * (format.getSampleSizeInBits() / 8) *
            format.getChannels();
        if (nbBytes > Integer.MAX_VALUE)
            throw new IllegalArgumentException("too many samples");
        // allocate a byte buffer
        byte[] inBuffer = new byte[(int)nbBytes];
        // read bytes from audio file
        audioInputStream.read(inBuffer, 0, inBuffer.length);
        // decode bytes into samples. Supported encodings are:
        // PCM-SIGNED, PCM-UNSIGNED, A-LAW, U-LAW
        decodeBytes(inBuffer, samples);
    }
    
    // Extract samples of a particular channel Samples, copy them into channelSamples Array
    public void getChannelSamples(int channel,
            double[] interleavedSamples, double[] channelSamples) {
        int nbChannels = format.getChannels();
        for (int i = 0; i < channelSamples.length; i++) {
            channelSamples[i] = interleavedSamples[nbChannels*i + channel];
        }
    }
    // Convenience method. Extract left and right channels for common stereo
    // files. leftSamples and rightSamples must be of size getSampleCount()
    public void getStereoSamples(double[] leftSamples, double[] rightSamples)
            throws IOException {
        long sampleCount = getSampleCount();
        double[] interleavedSamples = new double[(int)sampleCount*2];
        getInterleavedSamples(0, sampleCount, interleavedSamples);
        for (int i = 0; i < leftSamples.length; i++) {
            leftSamples[i] = interleavedSamples[2*i];
            rightSamples[i] = interleavedSamples[2*i+1];
        }        
    }

    // Private. Decode bytes of audioBytes into audioSamples
    private void decodeBytes(byte[] audioBytes, double[] audioSamples) {
        int sampleSizeInBytes = format.getSampleSizeInBits() / 8;
        int[] sampleBytes = new int[sampleSizeInBytes];
        int k = 0; // index in audioBytes
        for (int i = 0; i < audioSamples.length; i++) {
            // collect sample byte in big-endian order
            if (format.isBigEndian()) {
                // bytes start with MSB
                for (int j = 0; j < sampleSizeInBytes; j++) {
                    sampleBytes[j] = audioBytes[k++];
                }
            } else {
                // bytes start with LSB
                for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
                    sampleBytes[j] = audioBytes[k++];
                    if (sampleBytes[j] != 0)
                        j = j + 0;
                }
            }
            // get integer value from bytes
            int ival = 0;
            for (int j = 0; j < sampleSizeInBytes; j++) {
                ival += sampleBytes[j];
                if (j < sampleSizeInBytes - 1) ival <<= 8; // ival=ival<<8
            }
            /**
             * decode value
             * scale between -1, 1
             * sample = sample / 2^(bitsPerSample-1)
             */ 
             // val is the sample value after scaling
            double val = ((double)ival) / Math.pow(2., format.getSampleSizeInBits() - 1);

            audioSamples[i] = val;
            }
        }
    public double[] MinMaxArray(double[] samples) {
    	double mn = Double.MAX_VALUE, mx = Double.MIN_VALUE;
    	for (double d : samples) {
			if ( d > mx ) { mx = d; }
			else if (  d< mn ) { mn = d; }
		}
    	return new double[]{mn,mx};
    }
} // end class
