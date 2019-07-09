import java.io.*;
import java.util.*;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Framing {
	
	private double[] signal;
	private float wl_msec;
	private float inc_msec;
	double sampleRate;
	
	public Framing(double[] sig, float wl_ms, float inc_ms, double fs) {
		signal = sig;
		wl_msec = wl_ms;
		inc_msec = inc_ms;
		sampleRate = fs;
	}
	
	public int getWLinSamples() throws UnsupportedAudioFileException, IOException {
		// get the window length (frame size) in samples
		return (int) ((wl_msec / 1000.0) * sampleRate);
	}
	
	public int getINCinSamples() throws UnsupportedAudioFileException, IOException {
		//get the frame shift (increment) in samples
		return (int) ((inc_msec / 1000.0) * sampleRate);
	}
	
	public int getNoOfFullFrames() throws UnsupportedAudioFileException, IOException {
		int nx = signal.length;
		int wl = getWLinSamples();
		int inc = getINCinSamples(); 
		int nli = nx - wl + inc;
		return (int) Math.floor(nli/inc); // number of full frames
	}
	
	public double[][] getframes() throws UnsupportedAudioFileException, IOException {
//		System.out.println("overlap between frames is: " + ((float)inc_msec / wl_msec) * 100 + "%");
		int wl = getWLinSamples();
		int inc = getINCinSamples();
		int nf = getNoOfFullFrames();
		int[] indf = new int[nf]; // frames starts
		for (int i=0; i<indf.length; i++) {
			indf[i] = inc * i;
		}
		int j = 0;
		double[][] frames = new double[indf.length][wl];
		for (int i : indf) {
            int l=0;
		    for(int k=i; k<i+wl; k++) {
		        frames[j][l] = signal[k];
		        l++;
            }
			j++;
		}
		//System.out.println("Framing done... with dim ( " + indf.length + ", "+ wl + " )");
		return frames;
	}

}
