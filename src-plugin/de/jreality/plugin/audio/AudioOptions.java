package de.jreality.plugin.audio;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.audio.AudioAttributes;
import de.jreality.audio.DistanceCue;
import de.jreality.audio.SampleProcessor;
//import de.jreality.audio.jass.JassReverb;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.JSliderVR;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

/**
 * 
 * Plugin for setting basic audio parameters such as speed of sound and such
 * 
 * TODO: Implement AppearanceListener to update widgets in line with appearance changes.
 * 
 * @author brinkman
 *
 */
public class AudioOptions extends ShrinkPanelPlugin {

	private int[] selectedIndices = new int[0];
	private float speedOfSound = AudioAttributes.DEFAULT_SPEED_OF_SOUND;
	private float gain = AudioAttributes.DEFAULT_GAIN;
	private float reverbGain = AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN;
	private boolean reverbActive = false;
	
	private String[] cueLabels = {"constant", "conical", "low pass", "linear", "exponential"};
	private Class[] cueTypes = {DistanceCue.CONSTANT.class, DistanceCue.CONICAL.class, DistanceCue.LOWPASS.class, DistanceCue.LINEAR.class, DistanceCue.EXPONENTIAL.class};
	
	private Appearance rootAppearance;
	
	private JSliderVR gainWidget, speedWidget, reverbGainWidget;
	private JList distanceCueWidget;
	private JCheckBox reverbWidget;
	
	public AudioOptions() {
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		shrinkPanel.add(new JLabel("Distance cue"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(distanceCueWidget = new JList(cueLabels), gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		shrinkPanel.add(new JLabel("Speed of sound (m/s)"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(speedWidget = new JSliderVR(0, 1000), gbc);
		speedWidget.setPreferredSize(new Dimension(20, 50));
		speedWidget.setMajorTickSpacing(500);
		speedWidget.setPaintTicks(true);
		speedWidget.setPaintLabels(true);
		speedWidget.setPaintTrack(true);
		gbc.gridx = 0;
		gbc.gridy = 2;
		shrinkPanel.add(new JLabel("Gain (dB)"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(gainWidget = new JSliderVR(-60, 30, (int) toDecibels(gain)), gbc);
		gainWidget.setPreferredSize(new Dimension(20, 50));
		gainWidget.setMajorTickSpacing(30);
		gainWidget.setPaintTicks(true);
		gainWidget.setPaintLabels(true);
		gainWidget.setPaintTrack(true);
		gbc.gridx = 0;
		gbc.gridy = 3;
		shrinkPanel.add(new JLabel("Reverb"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(reverbWidget = new JCheckBox("active"), gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		shrinkPanel.add(new JLabel("Reverb gain (dB)"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(reverbGainWidget = new JSliderVR(-60, 30, (int) toDecibels(reverbGain)), gbc);
		reverbGainWidget.setPreferredSize(new Dimension(20, 50));
		reverbGainWidget.setMajorTickSpacing(30);
		reverbGainWidget.setPaintTicks(true);
		reverbGainWidget.setPaintLabels(true);
		reverbGainWidget.setPaintTrack(true);
		
		distanceCueWidget.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selectedIndices = distanceCueWidget.getSelectedIndices();
				setDistanceCueAttribute();
			}
		});
		speedWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				speedOfSound = speedWidget.getValue();
				setSpeedAttribute();
			}
		});
		gainWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gain = fromDecibels(gainWidget.getValue());
				setGainAttribute();
			}
		});
		reverbWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				reverbActive = reverbWidget.isSelected();
				setReverbAttribute();
			}
		});
		reverbGainWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				reverbGain = fromDecibels(reverbGainWidget.getValue());
				setReverbGainAttribute();
			}
		});
	}

	private void setDistanceCueAttribute() {
		List<Class<? extends DistanceCue>> list = new ArrayList<Class<? extends DistanceCue>>();
		for(int i: selectedIndices) {
			list.add((Class<? extends DistanceCue>) cueTypes[i]);
		}
		rootAppearance.setAttribute(AudioAttributes.DISTANCE_CUE_KEY, Collections.unmodifiableList(list));
	}

	private void setSpeedAttribute() {
		rootAppearance.setAttribute(AudioAttributes.SPEED_OF_SOUND_KEY, speedOfSound);
	}

	private void setGainAttribute() {
		rootAppearance.setAttribute(AudioAttributes.VOLUME_GAIN_KEY, gain);
	}
	
	private void setReverbAttribute() {
//		rootAppearance.setAttribute(AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, reverbActive ? JassReverb.class : SampleProcessor.class);
		rootAppearance.setAttribute(AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, SampleProcessor.class);
	}
	
	private void setReverbGainAttribute() {
		rootAppearance.setAttribute(AudioAttributes.DIRECTIONLESS_GAIN_KEY, reverbGain);
	}
	
	private void updateCueWidget() {
		distanceCueWidget.setSelectedIndices(selectedIndices);
	}
	
	private void updateSpeedWidget() {
		speedWidget.setValue((int) speedOfSound);
	}
	
	private void updateGainWidget() {
		gainWidget.setValue((int) toDecibels(gain));
	}
	
	private void updateReverbWidget() {
		reverbWidget.setSelected(reverbActive);
	}

	private void updateReverbGainWidget() {
		reverbGainWidget.setValue((int) toDecibels(reverbGain));
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Audio Options";
		info.vendorName = "Peter Brinkmann"; 
		info.icon = ImageHook.getIcon("Volume-Normal-Red-48x48.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		
		SceneGraphComponent root = c.getPlugin(View.class).getSceneRoot();
		rootAppearance = root.getAppearance();
		if (rootAppearance==null) {
			root.setAppearance(rootAppearance = new Appearance());
		}
	
		updateCueWidget();
		updateSpeedWidget();
		updateGainWidget();
		updateReverbWidget();
		updateReverbGainWidget();
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		
		try {
			selectedIndices = c.getProperty(getClass(), AudioAttributes.DISTANCE_CUE_KEY, new int[0]);
			speedOfSound = c.getProperty(getClass(), AudioAttributes.SPEED_OF_SOUND_KEY, AudioAttributes.DEFAULT_SPEED_OF_SOUND);
			gain = c.getProperty(getClass(), AudioAttributes.VOLUME_GAIN_KEY, AudioAttributes.DEFAULT_GAIN);
			reverbActive = c.getProperty(getClass(), AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, false);
			reverbGain = c.getProperty(getClass(), AudioAttributes.DIRECTIONLESS_GAIN_KEY, AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
		c.storeProperty(getClass(), AudioAttributes.DISTANCE_CUE_KEY, selectedIndices);
		c.storeProperty(getClass(), AudioAttributes.SPEED_OF_SOUND_KEY, speedOfSound);
		c.storeProperty(getClass(), AudioAttributes.VOLUME_GAIN_KEY, gain);
		c.storeProperty(getClass(), AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, reverbActive);
		c.storeProperty(getClass(), AudioAttributes.DIRECTIONLESS_GAIN_KEY, reverbGain);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}
	
	private final double dbq = 20/Math.log(10);
	private float toDecibels(float q) {
		return (float) (Math.log(q)*dbq);
	}
	private float fromDecibels(float db) {
		return (float) Math.exp(db/dbq);
	}
}
