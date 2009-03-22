package de.jreality.audio;

import de.jreality.scene.data.SampleReader;

/**
 * A simple sample rate converter, to be used as a transparent plugin between AudioSource and audio renderer.
 * 
 * @author brinkman
 *
 */

public class ConvertingReader implements SampleReader {

	private SampleReader reader;
	private float inBuf[];
	
	private final int targetRate, sourceRate, sampleRate;
	private final float ratio;
	private int targetIndex = 0;
	private int sourceIndex = 0;
	private int samplesRead = 0;
	
	private Interpolation interpolation = new Interpolation.Cubic();
	
	
	public static SampleReader createReader(SampleReader reader, int targetRate) {
		return (reader.getSampleRate()==targetRate) ? reader : new ConvertingReader(reader, targetRate);
	}
	
	private ConvertingReader(SampleReader reader, int targetRate) {
		int sourceRate = reader.getSampleRate();
		int q = gcd(targetRate, sourceRate);
		this.reader = reader;
		this.sampleRate = targetRate;
		this.sourceRate = sourceRate/q;
		this.targetRate = targetRate/q;
		ratio = ((float) sourceRate)/((float) targetRate);
		inBuf = new float[this.sourceRate];
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void clear() {
		reader.clear();
		targetIndex = 0;
		sourceIndex = 0;
		samplesRead = 0;
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		if (samplesRead<sourceRate) {
			samplesRead += reader.read(inBuf, samplesRead, sourceRate-samplesRead);
			if (samplesRead<sourceRate) {
				return 0;
			}
		}
		
		int i;
		for(i = 0; i<nSamples; i++) {
			if (targetIndex == targetRate) {
				targetIndex = sourceIndex = 0;
				samplesRead = reader.read(inBuf, 0, sourceRate);
				if (samplesRead<sourceRate) {
					break;
				}
			}

			int j = (++targetIndex*sourceRate)/targetRate;
			while (sourceIndex<j) {
				interpolation.put(inBuf[sourceIndex++]);
			}
			buffer[initialIndex+i] = interpolation.get(targetIndex*ratio-j);
		}
		return i;
	}
	
	private int gcd(int m, int n) {
		while (n!=0) {
			int mm = m;
			m = n;
			n = mm % n;
		}
		return m;
	}
}
