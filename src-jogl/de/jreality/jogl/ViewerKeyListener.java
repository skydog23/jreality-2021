/*
 * Created on Jun 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl;

import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JColorChooser;
import javax.swing.KeyStroke;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import de.jreality.jogl.tools.ToolManager;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.soft.MouseTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ViewerKeyListener extends KeyAdapter {
	InteractiveViewer viewer;
	boolean motionToggle = false;
	boolean fullScreenToggle = false;
	HelpOverlay helpOverlay;	
	/**
	 * 
	 */
	public ViewerKeyListener(InteractiveViewer v) {
		super();
		viewer = v;
		//helpOverlay = new HelpOverlay(v);
		helpOverlay = v.getHelpOverlay();
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_2,0), "Toggle stereo/mono camera");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_3,0), "Cycle stereo modes");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_3,0), "Toggle perspective/orthographic camera");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_4,0), "Print frame rate");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,0), "Increase alpha (1-transparency)");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.SHIFT_DOWN_MASK), "Decrease alpha");
		//helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,0), "Toggle antialiasing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B,0), "Toggle backplane display");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B,InputEvent.SHIFT_DOWN_MASK), "Toggle selection bound display");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C,0), "Set polygon diffuse color in selected appearance");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.SHIFT_DOWN_MASK), "Set background color");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D,0), "Toggle force display lists");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E,0), "Encompass");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.SHIFT_DOWN_MASK), "Toggle edge drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F,0), "Activate fly tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.SHIFT_DOWN_MASK), "Toggle face drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_H,0), "Toggle display help");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_J,0), "Increase sphere radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_J,InputEvent.SHIFT_DOWN_MASK), "Decrease sphere radius");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_L,0), "Toggle lighting enabled");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_M,0), "Reset Matrices to default");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.SHIFT_DOWN_MASK), "Set default Matrices with current state");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P,0), "Toggle perspective/orthographic view");
//		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0), "Force render");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_R,0), "Activate rotation tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S,0), "Toggle smooth shading");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.SHIFT_DOWN_MASK), "Toggle sphere drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_T,0), "Activate translation tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_V,0), "Toggle vertex drawing");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_X,0), "Toggle transparency enabled");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y,0), "Activate selection tool");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE,0), "Toggle fullscreen mode");
		helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Quit");
		if ((viewer.getViewingComponent() instanceof GLCanvas))
			((GLDrawable) v.getViewingComponent()).addGLEventListener(helpOverlay);

	}

	public void keyPressed(KeyEvent e)	{
			//System.err.println("handling keyboard event");
			int on;
			//System.out.println("Alt + Meta: "+e.isAltDown()+" "+e.isMetaDown());
			double[] cm ;
			switch(e.getKeyCode())	{


				case KeyEvent.VK_2:		// toggle stereo
					if (e.isShiftDown()) break;
					Camera cam = CameraUtility.getCamera(viewer);
					cam.setStereo(!cam.isStereo());
					cam.update();
					viewer.render();
					break;
				
				case KeyEvent.VK_3:		// cycle among cross-eyes and hardware stereo
					if (e.isShiftDown()) break;
					int which = viewer.getStereoType()+1;
					which = (which + 1) % 4;
					viewer.setStereoType(which+1);
					viewer.render();
					break;

				case KeyEvent.VK_4:		// display frame rate
					//System.err.println("Frame rate:\t"+viewer.getRenderer().getFramerate());
					//System.err.println("Speed test:\t"+viewer.speedTest());
					if (e.isShiftDown()) break;
					if (viewer instanceof de.jreality.jogl.Viewer) viewer.speedTest();
					break;

				case KeyEvent.VK_5:		// print out frame buffer capabilities
					if (e.isShiftDown()) break;
					if (! (viewer.getViewingComponent() instanceof GLCanvas)) break;
					GLCanvas can = (GLCanvas) viewer.getViewingComponent();
					GL gl = can.getGL();
					GLU glu = can.getGLU();
					int[] vals = new int[64];
					gl.glGetIntegerv(GL.GL_AUX_BUFFERS, vals);
					System.err.println("Auxilliary buffers: "+vals[0]);
					gl.glGetIntegerv(GL.GL_RED_BITS, vals);
					System.err.println("Red bits: "+vals[0]);
					gl.glGetIntegerv(GL.GL_BLUE_BITS, vals);
					System.err.println("Blue bits: "+vals[0]);
					gl.glGetIntegerv(GL.GL_GREEN_BITS, vals);
					System.err.println("Green bits: "+vals[0]);
						byte[] bvals = new byte[64];
					gl.glGetBooleanv(GL.GL_STEREO, bvals);
					System.err.println("Stereo: "+bvals[0]);
					gl.glGetIntegerv(GL.GL_SAMPLE_BUFFERS, vals);
					System.err.println("Sample buffers: "+vals[0]);
					gl.glGetIntegerv(GL.GL_STENCIL_BITS, vals);
					System.err.println("Stencil bits: "+vals[0]);
					break;
	
				case KeyEvent.VK_8:		// toggle stereo
					if (e.isShiftDown()) break;
					motionToggle = !motionToggle;
					if (motionToggle)	viewer.getMotionManager().resumeMotions();
					else viewer.getMotionManager().stopMotions();
					break;
				
				case KeyEvent.VK_A:		// transparency
					modulateValueAdditive(CommonAttributes.TRANSPARENCY,  0.5, .05, 0.0, 1.0, e.isShiftDown());
					break;

//				case KeyEvent.VK_A:		//antialiasing
//					if (e.isShiftDown()) break;
//					toggleValue(CommonAttributes.ANTIALIASING_ENABLED);
//					break;

				case KeyEvent.VK_B:		// toggle backplane
					if (e.isShiftDown()) {
						viewer.getSelectionManager().setRenderSelection( !viewer.getSelectionManager().isRenderSelection());
						viewer.getSelectionManager().setRenderPick( !viewer.getSelectionManager().isRenderPick());
					} else
						viewer.toggleBackPlane();
					viewer.render();
					break;

				case KeyEvent.VK_C:		// select a color
					java.awt.Color color = JColorChooser.showDialog(viewer.getViewingComponent(), "Select background color",  null);
					if (e.isShiftDown())	
						viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, color);
					else viewer.getSelectionManager().getSelectedAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, color);
					viewer.render();
					viewer.render();
					break;
					
				case KeyEvent.VK_D:		// toggle use of display lists
					if (e.isShiftDown()) break;
					boolean useD = viewer.getRenderer().isUseDisplayLists();
					viewer.getRenderer().setUseDisplayLists(!useD);
					System.out.println("Using display lists: "+viewer.getRenderer().isUseDisplayLists());
					break;

				case KeyEvent.VK_E:		
					if (!e.isShiftDown()) {		//encompass
						CameraUtility.encompass2(viewer);
//						MouseTool mt = new MouseTool(viewer);
//						mt.encompass();
						viewer.render();
					} else						// toggle edge drawing
						toggleValue(CommonAttributes.EDGE_DRAW);
					break;

				case KeyEvent.VK_F:		// toggle face drawing
					if (e.isShiftDown())		toggleValue(CommonAttributes.FACE_DRAW);
					viewer.getToolManager().activateTool(ToolManager.CAMERA_FLY_TOOL);
					break;

				case KeyEvent.VK_H:		// toggle help
					if (e.isShiftDown()) break;
					helpOverlay.setVisible(!helpOverlay.isVisible());
					viewer.render();
					break;

				case KeyEvent.VK_J:		// line width
					modulateValue(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.5,!e.isShiftDown());
					break;

				case KeyEvent.VK_L:		// toggle lighting
					if (e.isShiftDown()) break;
					toggleValue(CommonAttributes.LIGHTING_ENABLED);
					break;

				case KeyEvent.VK_M:		// reset matrices
					if (e.isShiftDown()) SceneGraphUtilities.setDefaultMatrix(viewer.getSceneRoot());
					else  SceneGraphUtilities.resetMatrix(viewer.getSceneRoot());
					viewer.render();
					break;

//				case KeyEvent.VK_N:		
//					break;

				case KeyEvent.VK_P:		// toggle perspective
					if (e.isShiftDown()) break;
					boolean val = CameraUtility.getCamera(viewer).isPerspective();
					CameraUtility.getCamera(viewer).setPerspective(!val);
					viewer.render();
					break;

				case KeyEvent.VK_Q:		
					if (e.isShiftDown()) break;
//					((GLCanvas) viewer.getViewingComponent()).setNoAutoRedrawMode(false);
//					viewer.render();
					break;

				case KeyEvent.VK_R:		// activate translation tool
					viewer.getToolManager().activateTool(ToolManager.ROTATION_TOOL);
					break;
				
				case KeyEvent.VK_S:		//smooth shading
					if (e.isShiftDown()) toggleValue(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW);
					else toggleValue(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING);
					break;

				case KeyEvent.VK_T:		// activate translation tool
					viewer.getToolManager().activateTool(ToolManager.TRANSLATION_TOOL);
					break;
				
				case KeyEvent.VK_V:		// draw vertices
					if (e.isShiftDown()) break;
					toggleValue(CommonAttributes.VERTEX_DRAW);
					break;

				case KeyEvent.VK_W:		// line width
					modulateValue(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 1.0, !e.isShiftDown());
					break;

				case KeyEvent.VK_X:		// toggle fast and dirty
					toggleValue(CommonAttributes.TRANSPARENCY_ENABLED);
					// the following is unfortunately unsymmetric: the get and the set don't match
					// this means that if the fastAndDirty attribute has been set somewhere below the root, 
					// (which shouldn't happen!), then this will have unintended results.
//					// In general, however, it would be good to have "rendering hints" 
//					boolean fad;
//					Object foo;
//					foo = viewer.getSceneRoot().getAppearance().getAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED);
//					if (foo instanceof Boolean)		{
//						fad = ((Boolean) foo).booleanValue();
//						System.err.println("Read value of "+fad);
//					}
//					else fad = false;
//					viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED,!fad);
//					foo = viewer.getSceneRoot().getAppearance().getAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED);
//					if (foo instanceof Boolean)		{
//						fad = ((Boolean) foo).booleanValue();
//						System.err.println("After flipping: Read value of "+fad);
//					}
//					viewer.render();
					break;
					
				case KeyEvent.VK_Y:		// activate translation tool
					viewer.getToolManager().activateTool(ToolManager.SELECTION_TOOL);
					break;
				
				
				case KeyEvent.VK_ESCAPE:		// toggle lighting
					if (e.isShiftDown()) break;
					System.exit(0);
					break;

				case KeyEvent.VK_BACK_QUOTE:
					if (e.isShiftDown()) break;
					Frame frame = Frame.getFrames()[0];
				    //frame.dispose();
				    //System.err.println("Disposal complete");
					fullScreenToggle = !fullScreenToggle;
					//frame.setUndecorated(fullScreenToggle);
					//frame.show();
				    //System.err.println("Show complete");
					frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullScreenToggle ? frame : null);
			        break;
			}
		}

	/**
	 * @param string
	 * @param d
	 * @param e
	 * @param f
	 * @param g
	 * @param b
	 */
	private void modulateValueAdditive(String name, double def, double inc, double min, double max, boolean increase) {
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		double newVal = def;
		if (obj != null && obj instanceof Double)	{
			newVal = ((Double) obj).doubleValue();
			if (increase) newVal +=  inc;
			else newVal -= inc;
		}
		//System.err.println("Setting value "+name+"Object is "+obj+"New value is "+newVal);
		if (newVal < min) newVal = min;
		if (newVal > max) newVal = max;
		ap.setAttribute(name, newVal);
		
		viewer.render();		
	}

	private void toggleValue(String  name)	{
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		boolean newVal = true;
		if (obj != null && obj instanceof Boolean)	{
			newVal = !((Boolean) obj).booleanValue();
		}
		//System.err.println("Toggling property"+name+"Object is "+obj+"New value is "+newVal);
			
		ap.setAttribute(name, newVal);
		viewer.render();
	}

	double factor = 1.2;
	private void modulateValue(String name, double val, boolean increase)	{
		Appearance ap = viewer.getSelectionManager().getSelectedAppearance();
		if (ap == null) return;
		Object obj = ap.getAttribute(name);
		double newVal = val;
		if (obj != null && obj instanceof Double)	{
			newVal = ((Double) obj).doubleValue();
			if (increase) newVal *= factor;
			else newVal /= factor;
		}
		//System.err.println("Setting value "+name+"Object is "+obj+"New value is "+newVal);
			
		ap.setAttribute(name, newVal);
		
		viewer.render();		
	}

}
