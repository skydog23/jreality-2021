package de.jreality.audio;

/**
 * Simple low-pass filter, mostly as a proof of concept.
 * 
 * TODO: Implement a better algorithm than the current discretization of an RC circuit.
 * 
 * @author brinkman
 *
 */
public class LowPassFilter implements SampleReader {

	private SampleReader reader;
	private int sampleRate;
	private float cutoff;
	private float alpha;
	private float prevValue = 0.0f;
	private float samples[];
	
	public LowPassFilter(SampleReader reader, int sampleRate, float cutoff) {
		this.reader = reader;
		this.sampleRate = sampleRate;
		samples = new float[sampleRate];
		setCutoff(cutoff);
	}
	
	public void setCutoff(float cutoff) {
		this.cutoff = cutoff;
		double tau = 1/(2*Math.PI*cutoff);   // RC time constant
		alpha = (float) (1/(1+tau*sampleRate));
	}

	public float getCutoff() {
		return cutoff;
	}
	
	public void clear() {
		reader.clear();
		prevValue = 0.0f;
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		int nRead = reader.read(samples, 0, nSamples);
		
		for(int i = initialIndex; i<nRead; i++) {
			prevValue += alpha*(samples[i]-prevValue);
			buffer[i] = prevValue;
		}
		
		return nRead;
	}
}
