package de.jreality.audio;

import java.util.LinkedList;
import java.util.Queue;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Sound path with delay (for Doppler shifts and such)
 * 
 * Compensates for discrepancies between video and audio frame rate
 * by low-pass filtering position information
 * 
 * @author brinkman
 *
 */
public class DelayPath implements SoundPath {
	
	private float gain = DEFAULT_GAIN;
	private float speedOfSound = DEFAULT_SPEED_OF_SOUND;
	private Attenuation attenuation = DEFAULT_ATTENUATION;
	private static final float UPDATE_CUTOFF = 6f; // play with this parameter if audio gets choppy
	
	private SampleReader reader;
	private int sampleRate;
	private float gamma;
	
	private Queue<float[]> sourceFrames = new LinkedList<float[]>();
	private Queue<Matrix> sourcePositions = new LinkedList<Matrix>();
	private Matrix currentMicPosition;
	
	private LowPassFilter xFilter, yFilter, zFilter; // TODO: consider better interpolation
	private float xTarget, yTarget, zTarget;
	
	private int relativeTime = 0;
	private float[] currentFrame = null;

	
	public DelayPath(SampleReader reader, int sampleRate) {
		if (sampleRate<=0) {
			throw new IllegalArgumentException("sample rate must be positive");
		}
		if (reader==null) {
			throw new IllegalArgumentException("reader cannot be null");
		}
		this.reader = ConvertingReader.createReader(reader, sampleRate);
		this.sampleRate = sampleRate;
	
		xFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		yFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		zFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		
		updateParameters();
	}

	public void setProperties(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(VOLUME_GAIN_KEY, DEFAULT_GAIN);
		speedOfSound = eapp.getAttribute(SPEED_OF_SOUND_KEY, DEFAULT_SPEED_OF_SOUND);
		attenuation = (Attenuation) eapp.getAttribute(VOLUME_ATTENUATION_KEY, DEFAULT_ATTENUATION);
		
		updateParameters();
	}
	
	private void updateParameters() {
		gamma = (speedOfSound>0f) ? sampleRate/speedOfSound : 0f; // samples per distance
	}
	
	public int processFrame(SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		float[] newFrame = new float[frameSize];
		int nRead = reader.read(newFrame, 0, frameSize);
		sourceFrames.add(newFrame);
		
		sourcePositions.add(new Matrix(sourcePos));
		currentMicPosition = invMicPos;
		
		updateTarget();
		
		if (currentFrame!=null) {
			for(int j = 0; j<frameSize; j++) {
				encodeSample(enc, j);
			}
		} else { // first frame, need to initialize fields
			currentFrame = sourceFrames.remove();
			sourcePositions.remove();
			
			xFilter.initialize(xTarget);
			yFilter.initialize(yTarget);
			zFilter.initialize(zTarget);
		}
	
		return nRead;
	}

	private void encodeSample(SoundEncoder enc, int j) {
		float x = xFilter.nextValue(xTarget);
		float y = yFilter.nextValue(yTarget);
		float z = zFilter.nextValue(zTarget);
		float dist = (float) Math.sqrt(x*x+y*y+z*z);

		float time;
		while ((time = relativeTime-gamma*dist+0.5f)>=currentFrame.length) {
			relativeTime -= currentFrame.length;
			currentFrame = sourceFrames.remove();
			sourcePositions.remove();
			updateTarget();
		}
		
		if (time>=0f) {
			int index = (int) time;
			float fractionalTime = time-index;

			float v0 = currentFrame[index++];
			float v1 = (index<currentFrame.length) ? currentFrame[index] : sourceFrames.element()[0];
			float v = v0+fractionalTime*(v1-v0);

			enc.encodeSample(attenuation.attenuate(v*gain, dist), j, x, y, z);
		}
		
		relativeTime++;
	}
	
	private Matrix auxiliaryMatrix = new Matrix();
	private void updateTarget() {
		auxiliaryMatrix.assignFrom(sourcePositions.element());
		auxiliaryMatrix.multiplyOnLeft(currentMicPosition);
		
		// TODO: Adjust the next three lines to generalize to curved geometries
		xTarget = (float) auxiliaryMatrix.getEntry(0, 3);
		yTarget = (float) auxiliaryMatrix.getEntry(1, 3);
		zTarget = (float) auxiliaryMatrix.getEntry(2, 3);
	}
}
