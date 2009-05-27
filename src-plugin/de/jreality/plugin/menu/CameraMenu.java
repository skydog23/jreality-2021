package de.jreality.plugin.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.AbstractJrToggleAction;
import de.jreality.ui.viewerapp.actions.camera.LoadCameraPreferences;
import de.jreality.ui.viewerapp.actions.camera.SaveCameraPreferences;
import de.jreality.ui.viewerapp.actions.camera.ShiftEyeSeparation;
import de.jreality.ui.viewerapp.actions.camera.ShiftFieldOfView;
import de.jreality.ui.viewerapp.actions.camera.ShiftFocus;
import de.jreality.ui.viewerapp.actions.camera.ToggleStereo;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class CameraMenu extends Plugin {

	ViewerSwitch viewer;
	Component viewingComp;
	private Scene
		scene = null;
	private ClickWheelCameraZoomTool
		zoomTool = new ClickWheelCameraZoomTool();
	private LoadCameraPreferences loadAction;
	private SaveCameraPreferences saveAction;

	private AbstractJrToggleAction zoomToolAction = new AbstractJrToggleAction("Zoom Tool") {
		
		private static final long 
			serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			setZoomEnabled(isSelected());
		}
		
	};
	
	
	private JMenu createCameraMenu() {
		JMenu cameraMenu = new JMenu("Camera");
		cameraMenu.setMnemonic(KeyEvent.VK_C);

		zoomToolAction.setIcon(ImageHook.getIcon("zoom.png"));
		cameraMenu.add(zoomToolAction.createMenuItem());
		cameraMenu.add(new JMenuItem(new ShiftFieldOfView("Decrease FOV", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftFieldOfView("Increase FOV", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ShiftFocus("Decrease Focus", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftFocus("Increase Focus", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ShiftEyeSeparation("Decrease Eye Separation", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftEyeSeparation("Increase Eye Separation", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ToggleStereo("Toggle Stereo", viewer)));
		loadAction = new LoadCameraPreferences("Load Preferences", viewer);
        loadAction.setIcon(ImageHook.getIcon("film_go.png"));
		cameraMenu.add(loadAction.createMenuItem());
        saveAction = new SaveCameraPreferences("Save Preferences", viewer);
		saveAction.setIcon(ImageHook.getIcon("film_save.png"));
		cameraMenu.add(saveAction.createMenuItem());

		return cameraMenu;
	}

	
	private void setZoomEnabled(boolean enable) {
		scene.getSceneRoot().removeTool(zoomTool);
		if (enable) {
			scene.getSceneRoot().addTool(zoomTool);
		}
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Camera Menu");
	}
	
	@Override
	public void install(Controller c) throws Exception {
		View view = c.getPlugin(View.class);
		viewer = view.getViewer();
		scene = c.getPlugin(Scene.class);
		viewingComp = viewer.getViewingComponent();
		c.getPlugin(ViewMenuBar.class).addMenu(getClass(), 2, createCameraMenu());
		
		ViewToolBar vtb = c.getPlugin(ViewToolBar.class);
		
		vtb.addTool(getClass(), 5.0, loadAction.createToolboxItem());
		vtb.addTool(getClass(), 5.1, saveAction.createToolboxItem());
		vtb.addSeparator(getClass(), 5.2);
		vtb.addTool(getClass(), 1.25, zoomToolAction.createToolboxItem());
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		c.getPlugin(ViewMenuBar.class).removeAll(getClass());
		c.getPlugin(ViewToolBar.class).removeAll(getClass());
	}

}