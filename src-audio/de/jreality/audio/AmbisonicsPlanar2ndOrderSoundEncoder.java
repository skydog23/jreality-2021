package de.jreality.audio;

import java.util.Arrays;

/**
 * 
 * Sound encoder for second order planar Ambisonics; see http://www.muse.demon.co.uk/ref/speakers.html
 * for mathematical background.
 *
 */
public abstract class AmbisonicsPlanar2ndOrderSoundEncoder implements SoundEncoder {

	protected static final float W_SCALE = (float) Math.sqrt(0.5);
	protected float[] bw, bx, by, bu, bv;
	
	public void startFrame(int framesize) {
		if (bw == null || bw.length != framesize) {
			bw=new float[framesize];
			bx=new float[framesize];
			by=new float[framesize];
			bu=new float[framesize];
			bv=new float[framesize];
		} else {
			Arrays.fill(bw, 0f);
			Arrays.fill(bx, 0f);
			Arrays.fill(by, 0f);
			Arrays.fill(bu, 0f);
			Arrays.fill(bv, 0f);
		}
	}
	
	public abstract void finishFrame();

	public void encodeSample(float v, int idx, float x, float y, float z) {
		float rp2 = z*z+x*x;
		float rp = (float) Math.sqrt(rp2);
		float r = (float) Math.sqrt(rp2+y*y);

		if (rp>1e-6f) {
			// The point (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics.
			encodeAmbiSample(v/Math.max(r, 1), idx, -z/rp, -x/rp);
		}
	}

	protected void encodeAmbiSample(float v, int idx, float x, float y) {
		bw[idx] += v*W_SCALE;
		bx[idx] += v*x;
		by[idx] += v*y;
		bu[idx] += v*(x*x-y*y);
		bv[idx] += v*(2f*x*y);
	}
}