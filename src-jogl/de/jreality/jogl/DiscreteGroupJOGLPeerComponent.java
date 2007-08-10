package de.jreality.jogl;

import javax.media.opengl.GL;

import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SystemProperties;

/**
 * This is an alternative class for use in conjunction with special sorts of
 * scene graphs with many identical children, as one finds in discrete groups.
 * 
 * To activate use, run java with "-DdiscreteGroup.copycat=true" set
 * @author Charles Gunn
 *
 */
public class DiscreteGroupJOGLPeerComponent extends JOGLPeerComponent {

	boolean displayListDirty = true;
	boolean displayListDirtyUp = false;
	boolean childrenDLDirty = true;
	boolean isCopyCat = false,
		isTopCat = false,
		isCameraRepn = false,
		visibilityChanged = false;
	boolean insideDL = false;
	double[][] matrices = null;
	double minDistance = -1;
	double maxDistance = -1;
	boolean clipToCamera = true;
	int delay = 50;
	Geometry trojanHorse = null;
	SceneGraphPath w2camrepn = null;
	double[] world2CameraRepn = new double[16], camera2CameraRepn = new double[16];
	float[] m2w = new float[16];
	int framecount = -1;
	protected boolean[] matrixIsReflection = null;
	GL oldGL;
	public static int instanceCount;
	Geometry theDropBox;
	public DiscreteGroupJOGLPeerComponent()	{
		super();
		instanceCount++;
	}
	
	public DiscreteGroupJOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr)		{
		super(sgp, p, jr);
		instanceCount++;
	}
	
	@Override
	public void init(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr) {
		super.init(sgp,p,jr);
		trojanHorse = goBetween.originalComponent.getGeometry();
		isCopyCat = (trojanHorse != null &&  
				trojanHorse instanceof PointSet && 
				trojanHorse.getGeometryAttributes(SystemProperties.JOGL_COPY_CAT) != null);
		if (isCopyCat)	{
			theDropBox = (Geometry) trojanHorse.getGeometryAttributes(SystemProperties.JOGL_COPY_CAT);
			System.err.println("id is "+theDropBox);
			readMatrices();
		}
	}

	private void readMatrices() {
		matrices = ((PointSet) trojanHorse).getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		matrixIsReflection = new boolean[matrices.length];
		for (int i = 0; i<matrices.length; ++i)	matrixIsReflection[i] = Rn.determinant(matrices[i]) < 0.0;
//		System.err.println("Updating matrices length "+matrices.length);
	}

	@Override
	public void render() {
		if (!isVisible) {
			return;
		}
		insideDL = false;
		boolean	geometryDLDirty = false;
		if (children.size() == 0 && goBetween.peerGeometry == null) return;
		// evaluate display list situation
		// first consider whether display lists are currently being used at all
		if (isCopyCat) {
//			System.err.println("Matrices length "+((PointSet) trojanHorse).getVertexAttributes(Attribute.COLORS).size());
			if (matrices.length != ((PointSet) trojanHorse).getVertexAttributes(Attribute.COLORS).size()) {
				readMatrices();				
				displayListDirty = geometryDLDirty = true;			
			}
			if (oldGL != jr.globalGL)	{
				displayListDirty = geometryDLDirty = true;
				oldGL = jr.globalGL;
			}
		}
		if (jr.renderingState.componentDisplayLists  && jr.renderingState.useDisplayLists) {
			// next prerequisite for using display lists at this node or above is that geometry and children are clean
			if (goBetween.peerGeometry != null && goBetween.peerGeometry.displayListsDirty) 
				displayListDirty = geometryDLDirty = true;
			if (!jr.offscreenMode && isCopyCat)	{
				// we only care about the time when we are clipping to the camera (and are top copycat node)
//				theLog.fine("dld\tcld:\t"+displayListDirty+"\t"+childrenDLDirty+"\t"+goBetween.originalComponent.getName());
				if (isTopCat)	{
					if (displayListDirtyUp)	{
						propagateSGCDisplayListDirtyDown();
//						theLog.fine("Propagating display list dirty down "+goBetween.getOriginalComponent().getName());
					}
					if (System.currentTimeMillis() - currentTime > delay)	{
						currentTime = System.currentTimeMillis();
						displayListDirty = true;
//						theLog.fine("Delay exceeded"+goBetween.getOriginalComponent().getName());
					}
				}
				// we only use a display list if we are a copycat node
				if (!childrenDLDirty && !geometryDLDirty) { // && !jr.renderingState.insideDisplayList)	{
					if (!displayListDirty)	{
						jr.globalGL.glCallList(displayList);
						return;
					}
//					theLog.fine("Going into display list: "+goBetween.getOriginalComponent().getName());
					// TODO figure out why the following exception occurs 
					// (currently disabled by above the enclosing if() statement)
					if (jr.renderingState.insideDisplayList) {
						//throw new IllegalStateException("Already inside display list");
						printUpState();
					}
					if (displayList < 0) displayList = jr.globalGL.glGenLists(1);
					//else jr.globalGL.glDeleteLists(displayList, 0);
					jr.globalGL.glNewList(displayList, GL.GL_COMPILE);
					insideDL = jr.renderingState.insideDisplayList = true;
//					theLog.fine("Turning on dlist for "+goBetween.getOriginalComponent().getName());
				}	
			}
		}
		super.render();
		if (insideDL)	{
			jr.globalGL.glEndList();
			jr.globalGL.glCallList(displayList);
			displayListDirty = jr.renderingState.insideDisplayList = false;
		} else if (!isCopyCat) {
			if (goBetween.peerGeometry != null && !goBetween.peerGeometry.displayListsDirty) 
				geometryDLDirty = false;
			displayListDirty = childrenDLDirty || geometryDLDirty;			
		}
	}

	private void printUpState()	{
		theLog.info("dld\tcld:\t"+displayListDirty+"\t"+childrenDLDirty+"\t"+name);
		if (parent!=null)((DiscreteGroupJOGLPeerComponent) parent).printUpState();
	}
	int signature;
	double[] o2c, o2ndc;
	@Override
	protected void renderChildren() {
		if (!jr.offscreenMode && isCopyCat)		{
			signature = jr.renderingState.currentSignature;
			theLog.fine("In renderChildren()"+name);
			boolean isReflectionBefore = jr.renderingState.flipped; //cumulativeIsReflection;

			int nn = matrices.length;
			o2ndc = jr.context.getObjectToNDC();
			o2c = jr.context.getObjectToCamera();
			int count = 0;
			DiscreteGroupJOGLPeerComponent child = (DiscreteGroupJOGLPeerComponent) children.get(0);
			boolean vis = child.isVisible;
			child.goBetween.originalComponent.setVisible(true);
			for (int j = 0; j<nn; ++j)	{
				if (clipToCamera && !accept(matrices[j])) continue; 
				count++;
				cumulativeIsReflection = (isReflectionBefore ^ matrixIsReflection[j]);
				jr.globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
				if (cumulativeIsReflection != jr.renderingState.flipped)	{
					jr.renderingState.flipped  = cumulativeIsReflection;
				}
				pushTransformation(matrices[j]);
				child.render();
				popTransformation();
			}
			childrenDLDirty = child.isDisplayListDirty();
			child.goBetween.originalComponent.setVisible(vis);
			theDropBox.setName(Integer.toString(count));
			theLog.fine("Rendered "+count);
			jr.renderingState.flipped = isReflectionBefore;
			jr.globalGL.glFrontFace(jr.renderingState.flipped ? GL.GL_CW : GL.GL_CCW);
		} else {
			childrenDLDirty = false;		// think positive!
			int n = children.size();
			for (int i = 0; i<n; ++i)	{	
				JOGLPeerComponent child = children.get(i);					
				if (jr.pickMode)	jr.globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
				child.render();
				if ( ((DiscreteGroupJOGLPeerComponent) child).isDisplayListDirty()) childrenDLDirty = true;
				if (jr.pickMode)	jr.globalGL.glPopName();
			}
		}
	}
		
	@Override
	protected void pushTransformation(double[] m) {
		if (w2camrepn != null )	{
			// recalculate the matrix if we haven't yet done it this frame
			if (framecount != jr.frameCount) {
				w2camrepn.getInverseMatrix(world2CameraRepn);
				framecount = jr.frameCount;
				Rn.times(camera2CameraRepn,world2CameraRepn,  jr.renderingState.cameraToWorld);
				Rn.times(camera2CameraRepn, camera2CameraRepn , CameraUtility.inverseCameraOrientation.getArray());

//				System.err.println("Setting up final thing");
			}
			super.pushTransformation(camera2CameraRepn);
		} else super.pushTransformation(m);
	}

	private boolean isDisplayListDirty() {
		if (!isVisible) return false;
		return displayListDirty;
	}

	public void propagateSGCDisplayListDirtyUp()	{
		if (jr != null && jr.renderingState != null && !jr.renderingState.componentDisplayLists) return;
		displayListDirtyUp = true;
		if (parent != null && !isTopCat && !((DiscreteGroupJOGLPeerComponent) parent).displayListDirtyUp) { // && (!isCopyCat || !clipToCamera)) 
			((DiscreteGroupJOGLPeerComponent) parent).propagateSGCDisplayListDirtyUp();
		}	}

	public void propagateSGCDisplayListDirtyDown()	{
		if (jr != null && jr.renderingState != null && !jr.renderingState.componentDisplayLists) return;
//		System.err.println("In propagateDown "+goBetween.originalComponent.getName());
		displayListDirty = childrenDLDirty = true;
//		childlock.readLock();
		for (JOGLPeerComponent child: children){		
			((DiscreteGroupJOGLPeerComponent) child).propagateSGCDisplayListDirtyDown();
		}	
//		childlock.readUnlock();
		displayListDirtyUp = false;
	}

	@Override
	protected void propagateAppearanceChanged() {
		displayListDirty = childrenDLDirty = true;
		super.propagateAppearanceChanged();
	}

	@Override
	protected void updateShaders() {
		super.updateShaders();
		if (eAp == null )return;
		if (goBetween.originalComponent.getAppearance() != null)	{
			Object foo = goBetween.originalComponent.getAppearance().getAttribute("discreteGroup.cameraRep", SceneGraphPath.class);
			if (foo != null && foo instanceof SceneGraphPath) {
				w2camrepn = (SceneGraphPath) foo;
				LoggingSystem.getLogger(this).finer("Found path in "+w2camrepn);
				useTformCaching = false;
			} else w2camrepn = null;			
		}
		if (!isCopyCat) return;
		minDistance = eAp.getAttribute("discreteGroup.minDistance", minDistance);
		maxDistance = eAp.getAttribute("discreteGroup.maxDistance", maxDistance);
		clipToCamera = eAp.getAttribute("discreteGroup.clipToCamera", clipToCamera);
		System.err.println("Clip to camera is "+clipToCamera);
		isTopCat = (isCopyCat && !existsHigherCat());
		delay = eAp.getAttribute("discreteGroup.delay", delay);	
	}

	private boolean existsHigherCat()	{
		if (parent != null) 
			if (((DiscreteGroupJOGLPeerComponent) parent).isCopyCat) return true;
			else return ((DiscreteGroupJOGLPeerComponent)parent).existsHigherCat();
		else return false;
	}
	@Override
	protected void updateTransformationInfo() {
		propagateSGCDisplayListDirtyUp();
		super.updateTransformationInfo();
	}

	@Override
	void setDisplayListDirty() {
	    propagateSGCDisplayListDirtyUp();
		super.setDisplayListDirty();
	}

	@Override
	public void visibilityChanged(SceneGraphComponentEvent ev) {
		theLog.fine("Visibility changed: "+name);
		propagateSGCDisplayListDirtyUp();
		super.visibilityChanged(ev);
	}
	//o2ndc, o2c, minDistance, maxDistance, matrices[j], jr.renderingState.currentSignature)) continue;
	private double[] mat = new double[16];
	private double[] tmp2 = new double[4];
	double fudge = 1.2;
	protected  boolean accept(double[] m) { //double[] objectToNDC, double[] o2c, double minDistance, double maxDistance, double[] m, int signature) {
			Rn.times(mat, o2c, m);
			tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
			double d = Pn.distanceBetween(tmp2, Pn.originP3, signature);
			
			if (minDistance > 0.0 &&  d < minDistance) return true;
			if (maxDistance > 0 && d > maxDistance) return false;
			Rn.times(mat, o2ndc, m);
			tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
			Pn.dehomogenize(tmp2,tmp2);
			if (Math.abs(tmp2[0]) > fudge) return false;
			if (Math.abs(tmp2[1]) > fudge) return false;
			if (Math.abs(tmp2[2]) > 1.0) return false;
			return true;
		}




}
