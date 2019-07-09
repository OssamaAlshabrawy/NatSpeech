
public class GeneralParameters {
	final float window_len_msec = 32.0f; // window length = 32 millisecond
	final float frame_shift_msec = 16.0f; // frame shift = half of window length
	final double sampleRate = 8192.0;
	
	public float getWindow_len_msec() {
		return window_len_msec;
	}
	public float getFrame_shift_msec() {
	    return frame_shift_msec;
	}
	public double getSampleRate() {
	    return sampleRate;
	}

}
