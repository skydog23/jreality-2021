package de.jreality.plugin.content;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.basic.ViewPreferences.ColorPickerModeChangedListener;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.AppearanceInspector;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ContentAppearance extends SceneShrinkPanel implements ColorPickerModeChangedListener {

	public static final boolean DEFAULT_SHOW_POINTS = true;
	public static final boolean DEFAULT_POINTS_REFLECTING = true;
	public static final double DEFAULT_POINT_RADIUS = .5;
	public static final Color DEFAULT_POINT_COLOR = Color.blue;
	public static final boolean DEFAULT_SHOW_LINES = true;
	public static final boolean DEFAULT_LINES_REFLECTING = true;
	public static final double DEFAULT_TUBE_RADIUS = .4;
	public static final Color DEFAULT_LINE_COLOR = Color.red;
	public static final boolean DEFAULT_SHOW_FACES = true;
	public static final boolean DEFAULT_FACES_REFLECTING = true;
	public static final double DEFAULT_FACE_REFLECTION = .5;
	public static final double DEFAULT_LINE_REFLECTION = .3;
	public static final double DEFAULT_POINT_REFLECTION = .3;
	public static final Color DEFAULT_FACE_COLOR = Color.white;
	public static final boolean DEFAULT_TRANSPARENCY_ENABLED = false;
	public static final double DEFAULT_TRANSPARENCY = .7;
	public static final boolean DEFAULT_FACES_FLAT = false;
	public static final boolean DEFAULT_TUBES = true;
	public static final boolean DEFAULT_SPHERES = true;
	public static final String DEFAULT_TEXTURE = "none";
	public static final double DEFAULT_TEXTURE_SCALE = .5;

	private ViewPreferences
		viewPreferences = null;
	private AppearanceInspector 
		appearanceInspector = null;
	
	private HashMap<String, String> 
		textures = new HashMap<String, String>();

	public AppearanceInspector getPanel() {
		return appearanceInspector;
	}

	public ContentAppearance() {
		textures.put("1 None", null);
		textures.put("2 Metal Grid", "textures/boysurface.png");
		textures.put("3 Metal Floor", "textures/metal_basic88.png");
		textures.put("4 Chain-Link Fence", "textures/chainlinkfence.png");
		
		appearanceInspector = new AppearanceInspector();
		restoreDefaults();
		shrinkPanel.setShrinked(true);
	}
	double worldSize = 1.0;
	
	public void install(Scene scene) {
		Appearance contentApp = scene.getContentAppearance();
		contentApp.setAttribute(
				CommonAttributes.RADII_WORLD_COORDINATES,
				true
		);
		appearanceInspector.setAppearance(contentApp);
	}

	public void colorPickerModeChanged(int mode) {
		getPanel().setColorPickerMode(mode);
	}
	
	public void restoreDefaults() {
		appearanceInspector.setShowPoints(DEFAULT_SHOW_POINTS);
		appearanceInspector.setPointsReflecting(DEFAULT_POINTS_REFLECTING);
		appearanceInspector.setPointRadius(DEFAULT_POINT_RADIUS);
		appearanceInspector.setPointColor(DEFAULT_POINT_COLOR);
		appearanceInspector.setShowLines(DEFAULT_SHOW_LINES);
		appearanceInspector.setLinesReflecting(DEFAULT_LINES_REFLECTING);
		appearanceInspector.setTubeRadius(DEFAULT_TUBE_RADIUS);
		appearanceInspector.setLineColor(DEFAULT_LINE_COLOR);
		appearanceInspector.setShowFaces(DEFAULT_SHOW_FACES);
		appearanceInspector.setFacesReflecting(DEFAULT_FACES_REFLECTING);
		appearanceInspector.setFaceReflection(DEFAULT_FACE_REFLECTION);
		appearanceInspector.setLineReflection(DEFAULT_LINE_REFLECTION);
		appearanceInspector.setPointReflection(DEFAULT_POINT_REFLECTION);
		appearanceInspector.setFaceColor(DEFAULT_FACE_COLOR);
		appearanceInspector.setTransparencyEnabled(DEFAULT_TRANSPARENCY_ENABLED);
		appearanceInspector.setTransparency(DEFAULT_TRANSPARENCY);
		appearanceInspector.setFacesFlat(DEFAULT_FACES_FLAT);
		appearanceInspector.setTubes(DEFAULT_TUBES);
		appearanceInspector.setSpheres(DEFAULT_SPHERES);
		appearanceInspector.setTexture(DEFAULT_TEXTURE);
		appearanceInspector.setTextureScale(DEFAULT_TEXTURE_SCALE); 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		appearanceInspector.setShowPoints(c.getProperty(getClass(), "showPoints", DEFAULT_SHOW_POINTS));
		appearanceInspector.setPointsReflecting(c.getProperty(getClass(), "pointsReflecting", DEFAULT_POINTS_REFLECTING));
		appearanceInspector.setPointRadius(c.getProperty(getClass(), "pointRadius", DEFAULT_POINT_RADIUS));
		appearanceInspector.setPointColor(c.getProperty(getClass(), "pointColor", DEFAULT_POINT_COLOR));
		appearanceInspector.setShowLines(c.getProperty(getClass(), "showLines", DEFAULT_SHOW_LINES));
		appearanceInspector.setLinesReflecting(c.getProperty(getClass(), "linesReflecting", DEFAULT_LINES_REFLECTING));
		appearanceInspector.setTubeRadius(c.getProperty(getClass(), "tubeRadius", DEFAULT_TUBE_RADIUS));
		appearanceInspector.setLineColor(c.getProperty(getClass(), "lineColor", DEFAULT_LINE_COLOR));
		appearanceInspector.setShowFaces(c.getProperty(getClass(), "showFaces", DEFAULT_SHOW_FACES));
		appearanceInspector.setFacesReflecting(c.getProperty(getClass(), "facesReflecting", DEFAULT_FACES_REFLECTING));
		appearanceInspector.setFaceReflection(c.getProperty(getClass(), "faceReflection", DEFAULT_FACE_REFLECTION));
		appearanceInspector.setLineReflection(c.getProperty(getClass(), "lineReflection", DEFAULT_LINE_REFLECTION));
		appearanceInspector.setPointReflection(c.getProperty(getClass(), "pointReflection", DEFAULT_POINT_REFLECTION));
		appearanceInspector.setFaceColor(c.getProperty(getClass(), "faceColor", DEFAULT_FACE_COLOR));
		appearanceInspector.setTransparencyEnabled(c.getProperty(getClass(), "transparencyEnabled", DEFAULT_TRANSPARENCY_ENABLED));
		appearanceInspector.setTransparency(c.getProperty(getClass(), "transparency", DEFAULT_TRANSPARENCY));
		appearanceInspector.setFacesFlat(c.getProperty(getClass(), "facesFlat", DEFAULT_FACES_FLAT));
		appearanceInspector.setTubes(c.getProperty(getClass(), "tubes", DEFAULT_TUBES));
		appearanceInspector.setSpheres(c.getProperty(getClass(), "spheres", DEFAULT_SPHERES));
		appearanceInspector.setTextures(c.getProperty(getClass(), "textures", textures));
		appearanceInspector.setTexture(c.getProperty(getClass(), "texture", DEFAULT_TEXTURE));
		appearanceInspector.setTextureScale(c.getProperty(getClass(), "textureScale", DEFAULT_TEXTURE_SCALE));
		super.restoreStates(c);
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "showPoints", appearanceInspector.isShowPoints());
		c.storeProperty(getClass(), "pointsReflecting", appearanceInspector.isPointsReflecting());
		c.storeProperty(getClass(), "pointRadius", appearanceInspector.getPointRadius());
		c.storeProperty(getClass(), "pointColor", appearanceInspector.getPointColor());
		c.storeProperty(getClass(), "showLines", appearanceInspector.isShowLines());
		c.storeProperty(getClass(), "linesReflecting", appearanceInspector.isLinesReflecting());
		c.storeProperty(getClass(), "tubeRadius", appearanceInspector.getTubeRadius());
		c.storeProperty(getClass(), "lineColor", appearanceInspector.getLineColor());
		c.storeProperty(getClass(), "showFaces", appearanceInspector.isShowFaces());
		c.storeProperty(getClass(), "facesReflecting", appearanceInspector.isFacesReflecting());
		c.storeProperty(getClass(), "faceReflection", appearanceInspector.getFaceReflection());
		c.storeProperty(getClass(), "lineReflection", appearanceInspector.getLineReflection());
		c.storeProperty(getClass(), "pointReflection", appearanceInspector.getPointReflection());
		c.storeProperty(getClass(), "faceColor", appearanceInspector.getFaceColor());
		c.storeProperty(getClass(), "transparencyEnabled", appearanceInspector.isTransparencyEnabled());
		c.storeProperty(getClass(), "transparency", appearanceInspector.getTransparency());
		c.storeProperty(getClass(), "facesFlat", appearanceInspector.isFacesFlat());
		c.storeProperty(getClass(), "tubes", appearanceInspector.isTubes());
		c.storeProperty(getClass(), "spheres", appearanceInspector.isSpheres());
		c.storeProperty(getClass(), "textures", appearanceInspector.getTextures());
		c.storeProperty(getClass(), "texture", appearanceInspector.getTexture());
		c.storeProperty(getClass(), "textureScale", appearanceInspector.getTextureScale());
		super.storeStates(c);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		install(c.getPlugin(Scene.class));
		viewPreferences = c.getPlugin(ViewPreferences.class);
		viewPreferences.addColorPickerChangedListener(this);
		getPanel().setColorPickerMode(viewPreferences.getColorPickerMode());
		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(appearanceInspector); 
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		shrinkPanel.removeAll();
		viewPreferences.removeColorPickerChangedListener(this);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content Appearance";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("lupeblau.png");
		return info; 
	}
	
	
	@Override
	public String getHelpDocument() {
		return "ContentAppearance.html";
	}
	
	@Override
	public String getHelpPath() {
		return "../help/";
	}
	
	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}

}

