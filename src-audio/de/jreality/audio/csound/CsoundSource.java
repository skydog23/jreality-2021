package de.jreality.audio.csound;

import java.io.IOException;

import csnd.CppSound;
import csnd.Csound;
import csnd.CsoundFile;
import csnd.CsoundMYFLTArray;
import de.jreality.audio.RingBuffer;
import de.jreality.audio.RingBufferSource;
import de.jreality.util.Input;

/**
 * Audio source that uses Csound as its synthesis engine.
 * 
 * @author brinkman
 *
 */
public class CsoundSource extends RingBufferSource {

	private CppSound csnd = new CppSound();
	private CsoundFile csf = csnd.getCsoundFile();
	private CsoundMYFLTArray auxBuffer;
	private int ksmps;
	private int nchnls;
	private int bufSize;
	private float scale;
	
	public CsoundSource(String name, Input csd) throws IOException {
		super(name);
		csf.setCSD(csd.getContentAsString());
		initFields();
	}
	
	public CsoundSource(String name, Input orc, Input score) throws IOException {
		super(name);
		csf.setOrchestra(orc.getContentAsString());
		csf.setScore(score.getContentAsString());
		initFields();
	}
	
	private void initFields() {
		csf.setCommand("-n -d foo.orc foo.sco");
		csf.exportForPerformance();
		csnd.compile();
		ksmps = csnd.GetKsmps();
		nchnls = csnd.GetNchnls();
		bufSize = ksmps*nchnls;
		sampleRate = (int) csnd.GetSr();
		scale = (float) csnd.Get0dBFS();
		ringBuffer = new RingBuffer(sampleRate);
		auxBuffer = new CsoundMYFLTArray(bufSize);
	}

	public Csound getCsound() {
		return csnd;
	}
	
	@Override
	protected void reset() {
		csnd.RewindScore();
	}

	@Override
	protected void writeSamples(int n) {
		for(int i=0; i<n; i+=ksmps) {
			if (csnd.PerformKsmps()!=0) {
				state = State.STOPPED;
				reset();
				hasChanged = true;
			}
			auxBuffer.SetValues(0, bufSize, csnd.GetSpout());
			for(int j=0; j<ksmps; j++) {
				double v = 0;
				for(int k=j; k<bufSize; k+=ksmps) {
					v += auxBuffer.GetValue(k);
				}
				ringBuffer.write((float) (v/scale));
			}
		}
	}
}
