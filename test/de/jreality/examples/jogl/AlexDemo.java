/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.quasitiler.alexanderplatz.Alex3DModel;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class  AlexDemo extends InteractiveViewerDemo {
	SceneGraphComponent icokit;
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = new SceneGraphComponent();
		world.setTransformation(new Transformation());
		world.getTransformation().setRotation(-Math.PI/2., 1,0,0);
        SceneGraphComponent sgc = Alex3DModel.createRoot(6, true, true, true);
        SceneGraphComponent scaleComp = new SceneGraphComponent();
        Transformation t = new Transformation();
        t.setStretch(3.);
        scaleComp.setTransformation(t);
        scaleComp.addChild(sgc);
        world.addChild(scaleComp);
        return world;
	}


	public static void main(String argv[])	{
		AlexDemo test = new AlexDemo();
		test.begin();
	}


}
