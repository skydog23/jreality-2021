package de.jreality.plugin.basic;


/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;


/**
 * @author pinkall
 */
public class View extends SideContainerPerspective implements ChangeListener {

	private ViewerSwitch viewerSwitch;
	private RenderTrigger renderTrigger;
	private boolean autoRender = true;

	private RunningEnvironment runningEnvironment;

	public enum RunningEnvironment {
		PORTAL,
		PORTAL_REMOTE,
		DESKTOP
	};
	
	void init(Scene scene) {
		SceneGraphComponent root=scene.getSceneRoot();
		
		// determine running environment
		String environment = Secure.getProperty(SystemProperties.ENVIRONMENT, SystemProperties.ENVIRONMENT_DEFAULT);
		if ("portal".equals(environment)) {
			runningEnvironment = RunningEnvironment.PORTAL; 
		} else if ("portal-remote".equals(environment)) {
			runningEnvironment = RunningEnvironment.PORTAL_REMOTE;
		} else {
			runningEnvironment = RunningEnvironment.DESKTOP;
		}

		// retrieve autoRender & synchRender system properties
		String autoRenderProp = Secure.getProperty(SystemProperties.AUTO_RENDER, SystemProperties.AUTO_RENDER_DEFAULT);
		if (autoRenderProp.equalsIgnoreCase("false")) {
			autoRender = false;
		}
		if (autoRender) {
			renderTrigger = new RenderTrigger();
		}
		viewerSwitch = createViewerSwitch();

		if (autoRender) {
			renderTrigger.addViewer(viewerSwitch);
			renderTrigger.addSceneGraphComponent(root);
		}

		viewerSwitch.setSceneRoot(root);
		
		getContentPanel().setLayout(new GridLayout());
		getContentPanel().add(viewerSwitch.getViewingComponent());
		getContentPanel().setPreferredSize(new Dimension(800,600));
		getContentPanel().setMinimumSize(new Dimension(300, 200));
		
		// TODO: move this into Scene Plugin...
		
		if (runningEnvironment != RunningEnvironment.DESKTOP) {
			Camera cam = (Camera) scene.getCameraPath().getLastElement();
			cam.setNear(0.01);
			cam.setFar(1500);
			cam.setOnAxis(false);
			cam.setStereo(true);
			SceneGraphComponent camNode = scene.getCameraComponent();
			String headMoveTool;
			if (runningEnvironment == RunningEnvironment.PORTAL_REMOTE)
				headMoveTool = "de.jreality.tools.RemotePortalHeadMoveTool";
			else
				headMoveTool = "de.jreality.tools.PortalHeadMoveTool";
			try {
				Tool t = (Tool) Class.forName(headMoveTool).newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
				System.err.println("crating headMoveTool failed");
			}
			scene.getSceneRoot().addTool(new PickShowTool());
			scene.getAvatarComponent().addTool(new PointerDisplayTool());
		}
		
	}
	
	private ViewerSwitch createViewerSwitch() {
		// make viewerSwitch
		Viewer[] viewers = null;
		if (getRunningEnvironment() != RunningEnvironment.DESKTOP) {
			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);
			try {
				viewers = new Viewer[]{(Viewer) Class.forName(viewer).newInstance()};
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL+" "+SystemProperties.VIEWER_DEFAULT_SOFT); // de.jreality.portal.DesktopPortalViewer");
			String[] vrs = viewer.split(" ");
			List<Viewer> viewerList = new LinkedList<Viewer>();
			String viewerClassName;
			for (int i = 0; i < vrs.length; i++) {
				viewerClassName = vrs[i];
				try {
					Viewer v = (Viewer) Class.forName(viewerClassName).newInstance();
					viewerList.add(v);
				} catch (Exception e) { // catches creation problems - i. e. no jogl in classpath
					LoggingSystem.getLogger(this).info("could not create viewer instance of ["+viewerClassName+"]");
				} catch (NoClassDefFoundError ndfe) {
					System.out.println("Possibly no jogl in classpath!");
				} catch (UnsatisfiedLinkError le) {
					System.out.println("Possibly no jogl libraries in java.library.path!");
				}
			}
			viewers = viewerList.toArray(new Viewer[viewerList.size()]);
		}
		return new ViewerSwitch(viewers);
	}

	public ViewerSwitch getViewer()	{
		return viewerSwitch;
	}

	public RunningEnvironment getRunningEnvironment() {
		return runningEnvironment;
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof Scene) {
			Scene scene = (Scene) e.getSource();
			updateScenePaths(scene);
		}
	}

	private void updateScenePaths(Scene scene) {
		viewerSwitch.setCameraPath(scene.getCameraPath());	
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "View";
		info.vendorName = "Ulrich Pinkall"; 
		info.icon = ImageHook.getIcon("hausgruen.png");
		info.isDynamic = false;
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		Scene scene = c.getPlugin(Scene.class);
		init(scene);
		updateScenePaths(scene);
		scene.addChangeListener(this);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		Scene scene = c.getPlugin(Scene.class);
		scene.removeChangeListener(this);
		super.uninstall(c);
		if (autoRender) {
			renderTrigger.removeSceneGraphComponent(viewerSwitch.getSceneRoot());
			renderTrigger.removeViewer(viewerSwitch);
		}
		if (viewerSwitch != null) {
			viewerSwitch.dispose();
		}
	}

	public Icon getIcon() {
		return getPluginInfo().icon;
	}

	public String getTitle() {
		return "jReality";
	}

	public void setVisible(boolean visible) {

	}

	public SelectionManager getSelectionManager() {
		return SelectionManagerImpl.selectionManagerForViewer(getViewer());
	}

	RenderTrigger getRenderTrigger() {
		return renderTrigger;
	}

	public JMenu createViewerMenu() {
		JMenu menu = new JMenu("Viewer");
		final ViewerSwitch viewerSwitch = getViewer();
		String[] viewerNames = viewerSwitch.getViewerNames();
		ButtonGroup bgr = new ButtonGroup();
		for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
			final int index = i;
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
			new AbstractAction(viewerNames[index]) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					viewerSwitch.selectViewer(index);
					viewerSwitch.getCurrentViewer().renderAsync();
				}
			});
			item.setSelected(index==0);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
			bgr.add(item);
			menu.add(item);
		}
		
		return menu;
	}
	
}