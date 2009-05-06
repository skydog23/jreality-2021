package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.audio.Audio;
import de.jreality.plugin.audio.AudioLauncher;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.Shell;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.vr.Avatar;
import de.jreality.plugin.vr.Sky;
import de.jreality.plugin.vr.Terrain;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;


public class AudioExample extends Plugin {

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Audio Example", "Peter Brinkmann");
	}

	@Override
	public void install(Controller c) throws Exception {
		InputStream testSoundIn = Audio.class.getResourceAsStream("zoom.wav");
		Input wavFile = Input.getInput("Zoom", testSoundIn);
		final AudioSource source = new CachedAudioInputStreamSource("zoom", wavFile, true);
		SceneGraphComponent audioComponent = new SceneGraphComponent("monolith");
		audioComponent.setAudioSource(source);
		audioComponent.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().translate(0, 5, 0).scale(2, 4.5, .5).assignTo(audioComponent);
	
		ActionTool actionTool = new ActionTool("PanelActivation");
		actionTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (source.getState() == AudioSource.State.RUNNING) {
					source.pause();
				} else {
					source.start();
				}
			}
		});
		audioComponent.addTool(actionTool);
		audioComponent.addTool(new DraggingTool());
		c.getPlugin(AlignedContent.class).setContent(audioComponent);
	}

	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		File propertiesFile = new File("AudioExample.jrw");
		SimpleController controller = new SimpleController(propertiesFile);
		ViewMenuBar viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuItem(AudioExample.class, 20.0, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			{
				putValue(AbstractAction.NAME, "Exit");
			}
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		}, "File");
		controller.registerPlugin(viewMenuBar);
		controller.registerPlugin(new View());
		controller.registerPlugin(new ViewPreferences());
		controller.registerPlugin(new CameraStand());
		controller.registerPlugin(new Lights());
		controller.registerPlugin(new AlignedContent());
		controller.registerPlugin(new ContentAppearance());
		controller.registerPlugin(new Inspector());
		controller.registerPlugin(new Shell());
		controller.registerPlugin(new Sky());
		controller.registerPlugin(new Terrain());
		controller.registerPlugin(new Avatar());
		controller.registerPlugin(new DisplayOptions());
		controller.registerPlugin(new AudioOptions());
		controller.registerPlugin(new AudioLauncher());
		controller.registerPlugin(new AudioExample());
		controller.startup();
	}
	
}
