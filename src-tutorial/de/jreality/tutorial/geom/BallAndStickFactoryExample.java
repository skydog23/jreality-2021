package de.jreality.tutorial.geom;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * An example of using the {@link BallAndStickFactory}.
 * @author Charles Gunn
 *
 */public class BallAndStickFactoryExample {

	public static void main(String[] args)	{
	   BallAndStickFactory basf = new BallAndStickFactory(Primitives.sharedIcosahedron);
	   basf.setBallRadius(.04);
	   basf.setStickRadius(.02);
	   basf.setShowArrows(true);
	   basf.setArrowScale(.1);
	   basf.setArrowSlope(1.5);
	   basf.setArrowPosition(.7);
	   basf.update();
	   SceneGraphComponent tubedIcosa = basf.getSceneGraphComponent();
	   ViewerApp.display(tubedIcosa);
	}
}