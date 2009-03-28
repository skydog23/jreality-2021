package de.jreality.plugin.view;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.PickShowTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.GuiUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/**
 * 
 * Plugin for quickly setting and saving camera settings and cursor/picking
 * 
 * @author brinkman
 *
 */
public class DisplayOptions extends ShrinkPanelPlugin {

	private JCheckBox pickBox;
	private JButton loadButton;
	private JButton saveButton;
	
	private View view;
	private PickShowTool pickShowTool = new PickShowTool();
	
	public DisplayOptions() {
		shrinkPanel.setLayout(new GridLayout(3, 1));
		shrinkPanel.add(loadButton = new JButton("Load camera preferences"));
		shrinkPanel.add(saveButton = new JButton("Save camera preferences"));
		shrinkPanel.add(pickBox = new JCheckBox("Show pick in scene"));
		
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPreferences();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePreferences();
			}
		});
		pickBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPick(pickBox.isSelected());
			}
		});
	}
	
	private void setPick(boolean showPick) {
		Component frame = view.getViewer().getViewingComponent();
		if (showPick) {
			GuiUtility.hideCursor(frame);
		} else {
			GuiUtility.showCursor(frame);
		}
		
		SceneGraphComponent root = view.getViewer().getSceneRoot();
		if (showPick && !root.getTools().contains(pickShowTool)) {
			root.addTool(pickShowTool);
		}
		if (!showPick && root.getTools().contains(pickShowTool)) {
			root.removeTool(pickShowTool);
		}
	}
	
	private void savePreferences() {
		CameraUtility.savePreferences((Camera) view.getCameraPath().getLastElement());
	}

	private void loadPreferences() {
		CameraUtility.loadPreferences((Camera) view.getCameraPath().getLastElement());
		view.getViewer().renderAsync();
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Display Options";
		info.vendorName = "Peter Brinkmann"; 
		info.icon = ImageHook.getIcon("camera.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(CameraStand.class);
		view = c.getPlugin(View.class);
		setPick(pickBox.isSelected());
		loadPreferences();
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		pickBox.setSelected(c.getProperty(getClass(), "showPick", false));
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showPick", pickBox.isSelected());
	}
}
