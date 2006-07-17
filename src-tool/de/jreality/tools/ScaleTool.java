package de.jreality.tools;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.ToolContext;

/**
 * @author bleicher
 *
 */

public class ScaleTool extends DragEventTool{
	
	double[] translation;
	public ScaleTool(){
		super("ScaleActivation");
		addPointDragListener(new PointDragListener(){
			public void pointDragStart(PointDragEvent e) {	
			}
			public void pointDragged(PointDragEvent e) {	
				translation=Rn.subtract(null,e.getPosition(),pickPoint);
		        if(translation[3]==0) translation[3]=1;
			}
			public void pointDragEnd(PointDragEvent e) {
			}			
		});
		addLineDragListener(new LineDragListener(){
			public void lineDragStart(LineDragEvent e) {				
			}
			public void lineDragged(LineDragEvent e) {
				translation=e.getTranslation();
			}
			public void lineDragEnd(LineDragEvent e) {
			}			
		});
		addFaceDragListener(new FaceDragListener(){
			public void faceDragStart(FaceDragEvent e) {
			}
			public void faceDragged(FaceDragEvent e) {
				translation=e.getTranslation();
			}
			public void faceDragEnd(FaceDragEvent e) {
			}			
		});
	}
	
	boolean active;
	double[] pickPoint;
	double[] objCenter;
	SceneGraphComponent pickedSGC;
	Transformation oldSGCTrafo;
	
	public void activate(ToolContext tc){
		active=true;
		pickPoint=tc.getCurrentPick().getObjectCoordinates();
		super.activate(tc);
		pickedSGC=tc.getCurrentPick().getPickPath().getLastComponent();
		oldSGCTrafo=pickedSGC.getTransformation();
		if(oldSGCTrafo==null) oldSGCTrafo=new Transformation();
		objCenter=GeometryUtility.calculateBoundingBox(pickedSGC).getCenter();		
		Matrix mtx=new Matrix(oldSGCTrafo);
		mtx.invert();
		objCenter=mtx.multiplyVector(objCenter);
	}
	public void perform(ToolContext tc){
		if(!active) return;
		super.perform(tc);
		double factor=Rn.euclideanNorm(Rn.subtract(null,Rn.add(null,pickPoint,translation),objCenter))/Rn.euclideanNorm(Rn.subtract(null,pickPoint,objCenter));
		MatrixBuilder.euclidean(oldSGCTrafo).translate(objCenter).scale(factor).translate(Rn.times(null,-1,objCenter)).assignTo(pickedSGC);
	}
	public void deactivate(ToolContext tc){
		super.deactivate(tc);
		active=false;
	}
	
}
