package de.jreality.plugin.scene;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.View.RunningEnvironment;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.tools.ShipNavigationTool.PickDelegate;
import de.jreality.ui.JSliderVR;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel.MinSizeGridBagLayout;

public class Avatar extends Plugin implements ChangeListener {

	public static final double DEFAULT_SPEED = 4;

	private SceneGraphComponent avatar;
	private SceneGraphComponent cameraComponent;
	private ShipNavigationTool shipNavigationTool;
	private Tool headTool;
	private ShrinkPanel panel;
	private JSliderVR speedSlider;

	public Avatar() {
		panel = new ShrinkPanel("Avatar");
		panel.setShrinked(true);
		panel.setIcon(getPluginInfo().icon);
		panel.add(Box.createHorizontalStrut(5));
		panel.setLayout(new MinSizeGridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		
		JLabel gainLabel = new JLabel("Speed");
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(gainLabel, c);
		speedSlider = new JSliderVR(0, 3000, (int) (100 * DEFAULT_SPEED));
		speedSlider.setPreferredSize(new Dimension(200,26));
		speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setNavigationSpeed(getNavigationSpeed());
			}
		});
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(speedSlider, c);
	}

	private void createTools(RunningEnvironment environment) {
		boolean portal = environment == RunningEnvironment.PORTAL;
		boolean portalRemote = environment == RunningEnvironment.PORTAL_REMOTE;
		// navigation tool
		shipNavigationTool = new ShipNavigationTool();
		if (portal || portalRemote) {
			shipNavigationTool.setPollingDevice(false);
		}

		// head transformation tool
		if (!portal && !portalRemote) {
			headTool = new HeadTransformationTool();
		}
	}
	
	private void updateComponents(Scene scene) {
		avatar = scene.getAvatarComponent();
		cameraComponent = scene.getCameraComponent();
		if (cameraComponent != null) {
			Camera cam = cameraComponent.getCamera();
			if (cam != null) {
				cam.setFieldOfView(60);
				cam.setNear(0.1);
				cam.setFar(10000);
			}
			MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(cameraComponent);
		} else {
			System.out.println("Avatar.updateComponents(): CAMERA CMP == NULL");
		}
		if (avatar != null) {
			MatrixBuilder.euclidean().translate(0,0,20).assignTo(avatar);
		}
	}
	
	private void installTools() {
		if (avatar != null) avatar.addTool(shipNavigationTool);
		if (cameraComponent != null && headTool != null) cameraComponent.addTool(headTool);
	}
		
	private void uninstallTools() {
		if (avatar != null) avatar.removeTool(shipNavigationTool);
		if (cameraComponent != null && headTool != null) cameraComponent.removeTool(headTool);
	}
	
	public Component getPanel() {
		return panel;
	}

	public double getNavigationSpeed() {
		double speed = 0.01*speedSlider.getValue();
		return speed;
	}

	public void setNavigationSpeed(double navigationSpeed) {
		int speed = (int)(100*navigationSpeed);
		speedSlider.setValue(speed);
		if (shipNavigationTool != null) {
			shipNavigationTool.setGain(navigationSpeed);
		}
	}
	

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		createTools(c.getPlugin(View.class).getRunningEnvironment());
		Scene scene = c.getPlugin(Scene.class);
		updateComponents(scene);
		installTools();
		
		scene.addChangeListener(this);
		VRPanel vp = c.getPlugin(VRPanel.class);
		vp.addComponent(getClass(), panel, 4.0, "VR");
		
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		Scene scene = c.getPlugin(Scene.class);
		scene.removeChangeListener(this);
		uninstallTools();
		VRPanel vp = c.getPlugin(VRPanel.class);
		vp.removeAll(getClass());
	}


	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Avatar";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("vr/avatar.png");
		return info; 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setNavigationSpeed(c.getProperty(getClass(), "navigationSpeed", getNavigationSpeed()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "navigationSpeed", getNavigationSpeed());
		super.storeStates(c);
	}

	public void setPickDelegate(PickDelegate pickDelegate) {
		shipNavigationTool.setPickDelegate(pickDelegate);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof Scene) {
			Scene scene = (Scene) e.getSource();
			uninstallTools();
			updateComponents(scene);
			installTools();
		}
	}

}
