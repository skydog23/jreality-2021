package de.jreality.tutorial.gui;

import java.awt.GridBagLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tutorial.geom.ParametricSurfaceExample;
import de.jreality.ui.JSliderVR;
import de.varylab.jrworkspace.plugin.PluginInfo;

/** 
 * Extends {@link ParametricSurfaceExample}. Visualizes the associated family of 
 * the catenoid and helicoid. Adds a slider for the parameter alpha of the associated
 * family
 *
 * @author G. Paul Peters, 22.07.2009
 *
 */
public class SliderExample {
	
	/** This class implements the associated family of the Helicoid and Catenoid
	 *  
 	 * x = cos alpha sinh v sin u + sin alpha cosh v cos u
	 * y = -cos alpha sinh v cos u + sin alpha cosh v sin u 
	 * z = u cos alpha + v sin alpha
	 * 
	 * u \in [-Pi,Pi[
	 * v \in \R
	 * 
	 * alpha = 0 helicoid
	 * alpha = Pi/2 catenoid
	 * 
	 */
	public static class HelicoidCatenoid implements Immersion {
		
		//add a parameter to the surface
		private double alpha;

		public double getAlpha() {
			return alpha;
		}
		public void setAlpha(double alpha) {
			this.alpha = alpha;
		}
		
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[index]= Math.cos(alpha) * Math.sinh(v) * Math.sin(u) + Math.sin(alpha) * Math.cosh(v) * Math.cos(u);
			xyz[index+2]= -Math.cos(alpha) * Math.sinh(v) * Math.cos(u) + Math.sin(alpha) * Math.cosh(v) * Math.sin(u);
			xyz[index+1]= u * Math.cos(alpha) + v * Math.sin(alpha);
		}
		public int getDimensionOfAmbientSpace() { return 3;	}
		public boolean isImmutable() { return false; }
	};
	
	public static void main(String[] args) {
		final HelicoidCatenoid helicoidCatenoid = new HelicoidCatenoid();
		final ParametricSurfaceFactory psf = new ParametricSurfaceFactory(helicoidCatenoid);
		psf.setUMin(-Math.PI);psf.setUMax(Math.PI);psf.setVMin(-1);psf.setVMax(1);
		psf.setULineCount(31);psf.setVLineCount(10);
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		psf.update();
		
		SceneGraphComponent sgc = new SceneGraphComponent("Helicoid-Catenoid");
		sgc.setGeometry(psf.getIndexedFaceSet());
		
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentTools());
		v.setContent(sgc);

		//create a slider for the parameter alpha 
		final int steps=60;
		final JSliderVR slider=new JSliderVR(0,steps,0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				helicoidCatenoid.setAlpha( 2*Math.PI * slider.getValue()/ steps );
				psf.update();
			}
		});
		
		//wrap  the slider in a plugin in order to add it to the viewer
		ViewShrinkPanelPlugin plugin = new ViewShrinkPanelPlugin() {
			@Override
			public PluginInfo getPluginInfo() {
				return new PluginInfo("alpha");
			}
		};
		plugin.getShrinkPanel().setLayout(new GridBagLayout());
		plugin.getShrinkPanel().add(slider);
		v.registerPlugin(plugin);
		
		//Start the viewer
		v.startup();
	}

    
}
