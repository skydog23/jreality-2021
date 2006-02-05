/*
 * Author	gunn
 * Created on Nov 14, 2005
 *
 */
package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.shader.CommonAttributes;

public class BallAndStickFactory {
	 IndexedLineSet ils;
	 double stickRadius=.025, ballRadius=.05;
	 Color stickColor = Color.YELLOW, ballColor=Color.GREEN;
	 int signature = Pn.EUCLIDEAN;
	 SceneGraphComponent theResult;


	public BallAndStickFactory(IndexedLineSet i)	{
		super();
		ils = i;
	 }

	 public void update()	{
			SceneGraphComponent sticks = BallAndStickFactory.sticks(ils, stickRadius, signature);
			// we should allow the user to specify "real" balls, not via the appearance.
			SceneGraphComponent balls = new SceneGraphComponent();
			balls.setGeometry(ils);
			Appearance ap =  new Appearance();
			ap.setAttribute(CommonAttributes.FACE_DRAW, false);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
			ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, ballRadius);
			if (ballColor != null) ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, ballColor);
			balls.setAppearance(ap);
			theResult = new SceneGraphComponent();
			ap =  new Appearance();
			ap.setAttribute(CommonAttributes.FACE_DRAW, true);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			if (stickColor != null) ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, stickColor);
			sticks.setAppearance(ap);
			ap =  new Appearance();
			ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
			theResult.setAppearance(ap);
			theResult.addChild(sticks);
			theResult.addChild(balls);
	 }
	 
	 public void setStickRadius(double r)	{
		 stickRadius = r;
	 }
	 
	public void setBallColor(Color ballColor) {
		this.ballColor = ballColor;
	}

	public void setBallRadius(double ballRadius) {
		this.ballRadius = ballRadius;
	}

	public void setStickColor(Color stickColor) {
		this.stickColor = stickColor;
	}

	public void setSignature(int signature) {
		this.signature = signature;
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return theResult;
	}

	protected static SceneGraphComponent sticks(IndexedLineSet ifs, double rad, int signature)	{
		SceneGraphComponent sgc = new SceneGraphComponent();
		DataList vertices = ifs.getVertexAttributes(Attribute.COORDINATES);
		int n = ifs.getNumEdges();
		for (int i = 0; i<n; ++i)	{
			int[] ed = ifs.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			for (int j = 0; j<m-1; ++j)	{
				int k = ed[j];
				double[] p1 = vertices.item(k).toDoubleArray(null);	
				k = ed[j+1];
				double[] p2 = vertices.item(k).toDoubleArray(null);	
				SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, rad, null, signature);
				if (cc != null) sgc.addChild(cc);
			}
		}
		return sgc;
	}

}
