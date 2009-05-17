package de.jreality.plugin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.AbstractAction;

import de.jreality.plugin.audio.Audio;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.Background;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.ContentLoader;
import de.jreality.plugin.view.ContentTools;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.ManagedContent;
import de.jreality.plugin.view.ManagedContentGUI;
import de.jreality.plugin.view.Shell;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.view.ViewToolBar;
import de.jreality.plugin.view.ZoomTool;
import de.jreality.plugin.vr.Avatar;
import de.jreality.plugin.vr.HeadUpDisplay;
import de.jreality.plugin.vr.Sky;
import de.jreality.plugin.vr.Terrain;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class ContentViewerVR {
	private SimpleController controller;
	private ViewMenuBar viewMenuBar;
	private View view;
	private CameraStand cameraStand;
	private Lights lights;
	private Background background;
	private AlignedContent alignedContent;
	private ViewPreferences viewPreferences;
	private Inspector inspector;
	private Shell shell;
	private ContentAppearance contentAppearance;
	private Sky sky;
	private ContentTools contentTools;
	
	private Avatar avatar;
	private Terrain terrain;
	
	private HeadUpDisplay headUpDisplay;

	private Audio audio;
	
	private boolean useAudio = false;
	
	
	public ContentViewerVR(File propertiesFile, boolean withAudio) {
		this.useAudio = withAudio;
		controller = new SimpleController(propertiesFile);
		initPlugins();
	}
	
	public ContentViewerVR(InputStream propertiesIn, boolean withAudio) {
		this.useAudio = withAudio;
		controller = new SimpleController(propertiesIn);
		initPlugins();
	}
	
	public ContentViewerVR(boolean withAudio) {
		this.useAudio = withAudio;
		controller = new SimpleController(new File("ContentViewerVR.jrw"));
		initPlugins();
	}

	protected void initPlugins() {
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewerVR.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewerVR.class, 20.0, new ExitAction(), "File");
		
		view = new View();
		controller.registerPlugin(view);
		
		cameraStand = new CameraStand();
		controller.registerPlugin(cameraStand);
		
		lights = new Lights();
		controller.registerPlugin(lights);
		
		background = new Background();
		controller.registerPlugin(background);
		
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewerVR.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewerVR.class, 20.0, new ExitAction(), "File");
		controller.registerPlugin(viewMenuBar);
		
		alignedContent = new AlignedContent();
		controller.registerPlugin(alignedContent);
		
		viewPreferences =  new ViewPreferences();
		controller.registerPlugin(viewPreferences);
		
		inspector = new Inspector();
		controller.registerPlugin(inspector);
		
		shell = new Shell();
		controller.registerPlugin(shell);
		
		contentAppearance = new ContentAppearance();
		controller.registerPlugin(contentAppearance);
		
		sky = new Sky();
		controller.registerPlugin(sky);

		avatar = new Avatar();
		avatar.setShowPanel(false);
		controller.registerPlugin(avatar);
	
		terrain = new Terrain();
		controller.registerPlugin(terrain);
		
		if (useAudio) {
			audio = new Audio();
			controller.registerPlugin(audio);
		}
		
		contentTools = new ContentTools();
		controller.registerPlugin(contentTools);
		
		headUpDisplay = new HeadUpDisplay();
		controller.registerPlugin(headUpDisplay);
		
		controller.registerPlugin(new DisplayOptions());
		controller.registerPlugin(new ZoomTool());
		controller.registerPlugin(new ViewToolBar());
		controller.registerPlugin(new ManagedContent());
		controller.registerPlugin(new ContentLoader());
		controller.registerPlugin(new ManagedContentGUI());
	}
	
	
	
	public void registerPlugin(Plugin plugin) {
		controller.registerPlugin(plugin);
	}
	
	public void startup() {
		controller.startup();
	}
	
	public void setContent(SceneGraphComponent content) {
		alignedContent.setContent(content);
	}
	
	public void contentChanged() {
		alignedContent.contentChanged();
	}
	
	public ViewMenuBar getViewMenuBar() {
		return viewMenuBar;
	}

	public View getView() {
		return view;
	}

	public CameraStand getCameraStand() {
		return cameraStand;
	}

	public Lights getLights() {
		return lights;
	}

	public Background getBackground() {
		return background;
	}

	public AlignedContent getAlignedContent() {
		return alignedContent;
	}

	public ViewPreferences getViewPreferences() {
		return viewPreferences;
	}

	public Shell getShell() {
		return shell;
	}

	public ContentAppearance getContentAppearance() {
		return contentAppearance;
	}

	public Sky getSky() {
		return sky;
	}

	public ContentTools getContentTools() {
		return contentTools;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public Audio getAudio() {
		return audio;
	}
	
	public HeadUpDisplay getHeadUpDisplay() {
		return headUpDisplay;
	}
	
	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	
	public static void main(String[] args) {
		ContentViewerVR contentViewer = new ContentViewerVR(false);
		contentViewer.startup();
	}

	public static Viewer remoteMain(String[] args) {
		ContentViewerVR contentViewer = new ContentViewerVR(true);
		contentViewer.startup();
		return contentViewer.view.getViewer().getCurrentViewer();
	}

}
