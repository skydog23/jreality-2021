package de.jreality.audio;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.jreality.math.Matrix;
import de.jreality.scene.Appearance;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphPathObserver;
import de.jreality.scene.AudioSource.State;
import de.jreality.scene.data.SampleReader;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.AudioEvent;
import de.jreality.scene.event.AudioListener;
import de.jreality.scene.proxy.tree.EntityFactory;
import de.jreality.scene.proxy.tree.ProxyTreeFactory;
import de.jreality.scene.proxy.tree.SceneGraphNodeEntity;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Audio backend; collects audio sources and appearances from the scene graph, keeps them up to date,
 * and provides an audio processing callback.
 *
 */
public class AudioBackend extends UpToDateSceneProxyBuilder implements AppearanceListener {

	private int sampleRate;
	private SceneGraphPath microphonePath;
	private Matrix inverseMicrophoneMatrix = new Matrix();
	private float[] directionlessBuffer = null;
	private SampleReader directionlessReader;
	private RingBuffer ringBuffer;
	private SampleProcessor directionlessProcessor = null;
	private List<AudioTreeNode> audioSources = new CopyOnWriteArrayList<AudioTreeNode>(); // don't want to sync traversal
	private SceneGraphPathObserver rootAppearanceObserver = new SceneGraphPathObserver();
	private SceneGraphPath rootAppearancePath = new SceneGraphPath();

	
	public AudioBackend(SceneGraphComponent root, SceneGraphPath microphonePath, int sampleRate) {
		super(root);
		this.microphonePath = microphonePath;
		this.sampleRate = sampleRate;
		ringBuffer = new RingBuffer(sampleRate);
		directionlessReader = ringBuffer.createSampleReader(sampleRate);
		
		setEntityFactory(new EntityFactory() {
			{
				setUpdateAudioSource(true);
			}

			protected SceneGraphNodeEntity produceAudioSourceEntity(AudioSource g) {
				return new AudioSourceEntity(g);
			}
		});
		setProxyTreeFactory(new ProxyTreeFactory() {
			public void visit(AudioSource a) {
				proxyNode = new AudioTreeNode(a);
			}
		});
		super.createProxyTree();

		Appearance rootApp = root.getAppearance();
		if (rootApp==null) {
			root.setAppearance(rootApp = new Appearance());
		}
		rootAppearancePath.push(root);
		rootAppearancePath.push(rootApp);
		rootAppearanceObserver.setPath(rootAppearancePath);

		appearanceChanged(null);
		rootAppearanceObserver.addAppearanceListener(this);
	}

	public synchronized void appearanceChanged(AppearanceEvent ev) {
		EffectiveAppearance eapp = EffectiveAppearance.create(rootAppearancePath);
		Class<? extends SampleProcessor> clazz = (Class<? extends SampleProcessor>) eapp.getAttribute(AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, SampleProcessor.class);
		if (clazz==SampleProcessor.class) {
			directionlessProcessor = null;
		} else if (directionlessProcessor==null || directionlessProcessor.getClass()!=clazz) {
			try {
				directionlessReader.clear();
				directionlessProcessor = clazz.newInstance();
				directionlessProcessor.initialize(directionlessReader);
			} catch (Exception e) {
				directionlessProcessor = null;
				e.printStackTrace();
			}
		}
		if (directionlessProcessor!=null) {
			directionlessProcessor.setProperties(eapp);
		}
	}

	public synchronized void processFrame(SoundEncoder enc, int frameSize) {
		if (directionlessProcessor!=null) {
			if (directionlessBuffer==null || directionlessBuffer.length<frameSize) {
				directionlessBuffer = new float[frameSize];
			} else {
				Arrays.fill(directionlessBuffer, 0, frameSize, 0f);
			}
		}

		microphonePath.getInverseMatrix(inverseMicrophoneMatrix.getArray());

		enc.startFrame(frameSize);
		for (AudioTreeNode node : audioSources) {
			try {
				node.processFrame(enc, frameSize, directionlessBuffer);
			} catch (Exception e) { // keep rendering even if individual sources fail
				e.printStackTrace();
			}
		}
		if (directionlessProcessor!=null) {
			try {
				ringBuffer.write(directionlessBuffer, 0, frameSize);
				int nRead = directionlessProcessor.read(directionlessBuffer, 0, frameSize);
				for(int i=0; i<nRead; i++) {
					enc.encodeSample(directionlessBuffer[i], i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		enc.finishFrame();
	}

	private class AudioTreeNode extends SceneTreeNode implements AudioListener, AppearanceListener {

		private SoundPath soundPath;
		private Matrix currentPosition = new Matrix();
		private SceneGraphPath path;
		private SceneGraphPathObserver observer = new SceneGraphPathObserver();

		private boolean nodeActive = false;
		private boolean pathActive = false;

		protected AudioTreeNode(AudioSource audio) {
			super(audio);

			soundPath = new DelayPath(audio.createReader(), sampleRate);

			audio.addAudioListener(this);
			observer.addAppearanceListener(this);

			audioChanged(null);
		}

		void processFrame(SoundEncoder encoder, int frameSize, float[] directionlessBuffer) {
			if (nodeActive || pathActive) {
				if (path==null) {
					path = toPath();
					appearanceChanged(null);
					observer.setPath(path);
				}
				path.getMatrix(currentPosition.getArray());
				pathActive = soundPath.processFrame(encoder, frameSize, currentPosition, inverseMicrophoneMatrix, directionlessBuffer);
			}
		}

		public void audioChanged(AudioEvent ev) {
			nodeActive = ((AudioSource) getNode()).getState() == State.RUNNING;
		}

		public void appearanceChanged(AppearanceEvent ev) {
			if (path!=null) {
				soundPath.setProperties(EffectiveAppearance.create(path));
			}
		}
	}

	private class AudioSourceEntity extends SceneGraphNodeEntity implements AudioListener {

		protected AudioSourceEntity(SceneGraphNode node) {
			super(node);
		}

		public void audioChanged(AudioEvent ev) {
			// do nothing for the time being...
		}

		protected void addTreeNode(SceneTreeNode tn) {
			super.addTreeNode(tn);
			audioSources.add((AudioTreeNode) tn);
		}

		protected void removeTreeNode(SceneTreeNode tn) {
			super.removeTreeNode(tn);
			audioSources.remove((AudioTreeNode) tn);
		}
	}
}
