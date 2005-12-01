/*
 * Created on Aug 11, 2004
 *
  */
package de.jreality.jogl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.util.BufferUtils;
import net.java.games.jogl.util.GLUT;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.LabelSet;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.HeightFieldFactory;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;

/**
 * @author gunn
 *
 */
public class JOGLRendererHelper {

	public final static int PER_PART = 1;
	public final static int PER_FACE = 2;
	public final static int PER_VERTEX = 4;
	public final static int PER_EDGE = 8;

	static float [] backgroundColor = {0f, 0f, 0f, 1f};
	static float val = 1f;
	static float[][] unitsquare = {{val,val},{-val,val},{-val, -val},{val,-val}};
	public static void handleBackground(GLDrawable theCanvas, int width, int height, Appearance topAp)	{
			GL gl = theCanvas.getGL();
			Object bgo = null;
			if (topAp == null) return;
			for (int i = 0; i<6; ++i)	{
				gl.glDisable(i+GL.GL_CLIP_PLANE0);
			}
			if (topAp != null)	bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_COLOR);
			if (bgo != null && bgo instanceof java.awt.Color) backgroundColor = ((java.awt.Color) bgo).getComponents(null);
			else backgroundColor = CommonAttributes.BACKGROUND_COLOR_DEFAULT.getRGBComponents(null);
			gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], 0.0f); //bg[3] ); //white 
			
			boolean hasTexture = false, hasColors = false;
			double textureAR = 1.0;
			bgo =  topAp.getAttribute("backgroundTexture");
			if (bgo != null && bgo instanceof Texture2D)	{
				Texture2D tex = ((Texture2D) bgo);
				//Texture2DLoaderJOGL tl = Texture2DLoaderJOGL.FactoryLoader;
				//JOGLConfiguration.theLog.log(Level.INFO,"Texture: "+tex.getWidth()+" "+tex.getHeight());
				textureAR = tex.getImage().getWidth()/((double) tex.getImage().getHeight());
				Texture2DLoaderJOGL.render( theCanvas, tex);
				gl.glEnable(GL.GL_TEXTURE_2D);
				hasTexture = true;
			}
			double ar = width/((double) height)/textureAR;
			double xl=0, xr=1, yb=0, yt=1;
			if (ar > 1.0)	{ xl = 0.0; xr = 1.0; yb =.5*(1-1/ar);  yt = 1.0 - yb; }
			else 			{ yb = 0.0; yt = 1.0; xl =.5*(1-ar);  xr = 1.0 - xl; }
			double[][] texcoords = {{xl,yb },{xr,yb},{xr,yt},{xl,yt}};
			bgo =  topAp.getAttribute("backgroundColors");
			if (bgo != null && bgo instanceof Color[])	{
				hasColors = true;
			}
			if (hasTexture || hasColors)	{
				//bgo = (Object) corners;
				float[][] cornersf = new float[4][];
				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_LIGHTING);
				gl.glShadeModel(GL.GL_SMOOTH);
				gl.glBegin(GL.GL_POLYGON);
				//gl.glScalef(.5f, .5f, 1.0f);
				for (int q = 0; q<4; ++q)		{
					if (hasTexture)	{
						gl.glColor3f(1f, 1f, 1f);						
						gl.glTexCoord2dv(texcoords[q]);
					} else {
						cornersf[q] = ((Color[]) bgo)[q].getComponents(null);
						gl.glColor3fv(cornersf[q]);						
					}
					gl.glVertex2fv(unitsquare[q]);
				}
				gl.glEnd();
				gl.glEnable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			bgo = topAp.getAttribute(CommonAttributes.FOG_ENABLED);
			boolean doFog = CommonAttributes.FOG_ENABLED_DEFAULT;
			if (bgo instanceof Boolean) doFog = ((Boolean) bgo).booleanValue();
			if (doFog)	{
				gl.glEnable(GL.GL_FOG);
				bgo =  topAp.getAttribute(CommonAttributes.FOG_COLOR);
				float[] fogColor = backgroundColor;
				if (bgo != null && bgo instanceof Color)	{
					fogColor = ((Color) bgo).getRGBComponents(null);
				}
				gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
				gl.glFogfv(GL.GL_FOG_COLOR, fogColor);
				bgo =  topAp.getAttribute(CommonAttributes.FOG_DENSITY);
				float density = (float) CommonAttributes.FOG_DENSITY_DEFAULT;
				if (bgo != null && bgo instanceof Double)	{
					density = (float) ((Double) bgo).doubleValue();
				}
				gl.glFogf(GL.GL_FOG_DENSITY, density);
			} else gl.glDisable(GL.GL_FOG);

	}
	static boolean testArrays = false;
	static ByteBuffer vBuffer, vcBuffer, vnBuffer, fcBuffer, fnBuffer, tcBuffer;
	static DataList vLast = null, vcLast = null, vnLast = null;
	public static void drawVertices( PointSet sg, JOGLRenderer jr, boolean pickMode, double alpha) {
		GLDrawable theCanvas = jr.theCanvas;
		GL gl = theCanvas.getGL(); 
//		gl.glPointSize((float) currentGeometryShader.pointShader.getPointSize());
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList pointSize = sg.getVertexAttributes(Attribute.POINT_SIZE);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		int colorLength = 0;
		if (vertexColors != null) {
			colorLength = GeometryUtility.getVectorLength(vertexColors);
			if (jr.openGLState.frontBack != DefaultPolygonShader.FRONT_AND_BACK)	{
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK, GL.GL_DIFFUSE);
				jr.openGLState.frontBack =DefaultPolygonShader.FRONT_AND_BACK;
			}
		}
		
		DoubleArray da;
		if (pickMode)	gl.glPushName(JOGLPickAction.GEOMETRY_POINT);
		//if (pickMode) JOGLConfiguration.theLog.log(Level.INFO,"Rendering vertices in picking mode");
		if (!pickMode) gl.glBegin(GL.GL_POINTS);
		for (int i = 0; i< sg.getNumPoints(); ++i)	{
			//double vv;
			if (pickMode) gl.glPushName(i);
			if (pickMode) gl.glBegin(GL.GL_POINTS);
			if (pointSize != null) {
				float ps = (float) pointSize.item(i).toDoubleArray().getValueAt(0);
				gl.glPointSize( ps);
				//vv =  (ps < 1) ? ps : (1d - (Math.ceil(ps) - ps) * 0.25d);

			}
			//if (pointSize != null)	gl.glBegin(GL.GL_POINTS);
			if (vertexColors != null)	{
				da = vertexColors.item(i).toDoubleArray();
				if (colorLength == 3) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
				} 
			}
			da = vertices.item(i).toDoubleArray();				
			if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
			else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
			if (pickMode) gl.glEnd();
			if (pickMode) gl.glPopName();
		}
		if (!pickMode) gl.glEnd();
		if (pickMode) gl.glPopName();
	}
	/**
	 * @param sg
	 */
	public static void drawLines(IndexedLineSet sg, JOGLRenderer jr , boolean pickMode, boolean interpolateVertexColors, double alpha) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		DataList edgeColors = sg.getEdgeAttributes(Attribute.COLORS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		boolean hasNormals = vertexNormals == null ? false : true;
		DoubleArray da;
		//SJOGLConfiguration.theLog.log(Level.INFO,"Processing ILS");

//        if (testArrays) {
//                   double[] varray = vertices.toDoubleArray(null);
//                   ByteBuffer bb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
//                   bb.asDoubleBuffer().put(varray);
//                   bb.flip();
//                   gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, bb);
// 
//                   gl.glDisableClientState(GL.GL_COLOR_ARRAY);
//                   gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
//                   gl.glDrawArrays(GL.GL_POINTS, 0, sg.getNumPoints());
// 
//                   for (int i = 0; i sg.getNumEdges(); ++i)       {
//                        gl.glBegin(GL.GL_LINE_STRIP);
//                        IntArray ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
//                        int m = ed.getLength();
//                        for (int j = 0; jm; ++j)       {
//                            gl.glArrayElement(ed.getValueAt(j));
//                         }
//                    gl.glEnd();
//              }
		if (sg.getEdgeAttributes(Attribute.INDICES) == null) return;
		int colorBind = 0, colorLength = 0;
		if (interpolateVertexColors && vertexColors != null) 		{
			colorBind = PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} 
		else if (edgeColors != null) 	{
			colorBind = PER_EDGE;
			colorLength = GeometryUtility.getVectorLength(edgeColors);
		} 
		else 	colorBind = PER_PART;
		if (colorBind != PER_PART)	{
			if (jr.openGLState.frontBack != DefaultPolygonShader.FRONT_AND_BACK)	{
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK, GL.GL_DIFFUSE);
				jr.openGLState.frontBack =DefaultPolygonShader.FRONT_AND_BACK;
			}			
		}
		//pickMode = false;
		if (pickMode)	gl.glPushName(JOGLPickAction.GEOMETRY_LINE);
		int numEdges = sg.getNumEdges();
		//if (pickMode) JOGLConfiguration.theLog.log(Level.INFO,"Rendering edges in picking mode");
		for (int i = 0; i< numEdges; ++i)	{
			if (pickMode)	gl.glPushName(i);
			if (!pickMode) gl.glBegin(GL.GL_LINE_STRIP);
			int[] ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			if (!pickMode && colorBind == PER_EDGE) 		{	
				da = edgeColors.item(i).toDoubleArray();
				if (colorLength == 3) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
				} 
			}

			for (int j = 0; j<m; ++j)	{
				if (pickMode)	{
					if (j == m-1) break;
					gl.glPushName(j);
					gl.glBegin(GL.GL_LINES);
				}
				int k = ed[j];
				if (!pickMode && colorBind == PER_VERTEX) 		{	
					da = vertexColors.item(k).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
					} else if (colorLength == 4) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
					} 
				}
				if (hasNormals)	{
					da = vertexNormals.item(k).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				}
				da = vertices.item(k).toDoubleArray();		
				if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
				//if (pickMode)	gl.glPopName();
				if (pickMode)	{
					k = ed[j+1];
					da = vertices.item(k).toDoubleArray();				
					if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
					gl.glEnd();
					gl.glPopName();
				}
			}
			if (!pickMode) 	gl.glEnd();
			if (pickMode)	gl.glPopName();
		}
		if (pickMode)	gl.glPopName();
//		gl.glDepthRange(0d, 1d);
	}

	public static void drawFaces( IndexedFaceSet sg,JOGLRenderer jr,  boolean smooth, double alpha) {
		drawFaces(sg, jr, smooth, alpha, false);
	}
	public static void drawFaces( IndexedFaceSet sg, JOGLRenderer jr,  boolean smooth, double alpha, boolean pickMode) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		
		int colorBind = -1,normalBind, colorLength=3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		DataList lightMapCoords = sg.getVertexAttributes(Attribute.attributeForName("lightmap coordinates"));
		//JOGLConfiguration.theLog.log(Level.INFO,"Vertex normals are: "+((vertexNormals != null) ? vertexNormals.size() : 0));
		//JOGLConfiguration.theLog.log(Level.INFO,"alpha value is "+alpha);
		
		// signal a geometry
		if (pickMode)	gl.glPushName(JOGLPickAction.GEOMETRY_FACE); //pickName);
		
		// vertex color has priority over face color
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		if (vertexColors != null && smooth) 		{
			colorBind = PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} 
		else if (faceColors != null && colorBind != PER_VERTEX) 	{
			colorBind = PER_FACE;
			colorLength = GeometryUtility.getVectorLength(faceColors);
		} 
		else 	colorBind = PER_PART;
		//JOGLConfiguration.theLog.log(Level.INFO,"Color binding is "+colorBind);
		if (colorBind != PER_PART)	{
			if (jr.openGLState.frontBack != DefaultPolygonShader.FRONT_AND_BACK)	{
				gl.glEnable(GL.GL_COLOR_MATERIAL);
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK, GL.GL_DIFFUSE);
				jr.openGLState.frontBack =DefaultPolygonShader.FRONT_AND_BACK;
			}			
		}
		if (vertexNormals != null && smooth)	{
				normalBind = PER_VERTEX;
			}
		else 	if (faceNormals != null  && (vertexNormals == null || !smooth)) {
				normalBind = PER_FACE;
		}
		else normalBind = PER_PART;
		
//		if (vertices != null)	{
//			int vlength = GeometryUtility.getVectorLength(vertices);
//			JOGLConfiguration.theLog.log(Level.INFO,"Vertics have length "+vlength);			
//		}
//		if (faceNormals != null)	{
//			int vlength = GeometryUtility.getVectorLength(faceNormals);
//			JOGLConfiguration.theLog.log(Level.INFO,("Normals have length "+vlength);			
//		}
		DoubleArray da;
		boolean isQuadMesh = false;
		boolean isRegularDomainQuadMesh = false;
		Rectangle2D theDomain = null;
		int maxU = 0, maxV = 0, maxFU = 0, maxFV = 0, numV = 0, numF;
		Object qmatt = sg.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
		if (qmatt != null && qmatt instanceof Dimension)	{
			Dimension dm = (Dimension) qmatt;
			isQuadMesh = true;
			maxU = dm.width;
			maxV = dm.height;
			numV = maxU * maxV;
			maxFU = maxU-1;
			maxFV = maxV-1;
			// Done with GeometryAttributes?
			qmatt = sg.getGeometryAttributes(GeometryUtility.REGULAR_DOMAIN_QUAD_MESH_SHAPE);
			if (qmatt != null && qmatt instanceof Rectangle2D)	{
				theDomain = (Rectangle2D) qmatt;
				isRegularDomainQuadMesh = true;
			}				
		} else if (sg instanceof QuadMeshShape) {
			QuadMeshShape qm = (QuadMeshShape) sg;
			isQuadMesh = true;
			maxU = qm.getMaxU();
			maxV = qm.getMaxV();
			numV = maxU * maxV;
			maxFU = qm.isClosedInUDirection() ? maxU : maxU - 1;
			maxFV = qm.isClosedInVDirection() ? maxV : maxV - 1;
			numF = qm.getNumFaces();
		}

		numF = sg.getNumFaces();
		if (!pickMode && isQuadMesh)	{
			//System.out.println("Is quad mesh");
//			RegularDomainQuadMesh rdqm = null;
//			if (qm instanceof RegularDomainQuadMesh) {
//				rdqm = (RegularDomainQuadMesh) qm;
//				type = rdqm.getType();
//			}
			double[] pt = new double[3];
			// this loops through the "rows" of  the mesh (v is constant on each row)
			for (int i = 0; i< maxFV ; ++i)	{
				if (pickMode) gl.glPushName(i);
				gl.glBegin(GL.GL_QUAD_STRIP);
				// each iteration of this loop draws one quad strip consisting of 2 * (maxFU + 1) vertices
				for (int j = 0; j <= maxFU; ++j)	{
					int u = j%maxU;
//					if (pickMode) {
//						//JOGLConfiguration.theLog.log(Level.INFO,"+G"+faceCount+"\n");
//						gl.glPushName(faceCount++);
//					}
					// draw two points: one on "this" row, the other directly below on the next "row"
					for (int incr = 0; incr< 2; ++incr)	{
						int vnn = (i*maxU + j%maxU + incr*maxU)%numV;
						int fnn = (i*maxFU + j%maxFU + incr*maxFU)%numF;
						int v = (i+incr)%maxV;
						if (normalBind == PER_FACE) {
							if (incr == 0 && j != maxFU) 	{
								da = faceNormals.item(fnn).toDoubleArray();
								gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));								
							}
						} else 
						if (normalBind == PER_VERTEX) {
							da = vertexNormals.item(vnn).toDoubleArray();
							gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
						} 
						if (colorBind == PER_FACE) 		{	
							if (incr == 0) {
								da = faceColors.item(fnn).toDoubleArray();
								if (colorLength == 3) 	{
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
								} else if (colorLength == 4) 	{
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
								} 
							}
						} else
						if (colorBind == PER_VERTEX) {
							da = vertexColors.item(vnn).toDoubleArray();
							if (colorLength == 3) 	{
								gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
							} else if (colorLength == 4) 	{
								gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
							} 
						}
						if (texCoords != null)	 {
							da = texCoords.item(vnn).toDoubleArray();
							gl.glMultiTexCoord2d(GL.GL_TEXTURE0, da.getValueAt(0), da.getValueAt(1));
						}
						if (lightMapCoords != null) {
							da = lightMapCoords.item(vnn).toDoubleArray();
							gl.glMultiTexCoord2d(GL.GL_TEXTURE1, da
									.getValueAt(0), da.getValueAt(1));
						}
						da = vertices.item(vnn).toDoubleArray();
						if (vertexLength == 1 && isRegularDomainQuadMesh)	{		// Regular domain quad mesh
							double z = da.getValueAt(0);
							HeightFieldFactory.getCoordinatesForUV(pt, theDomain, u, v, maxU, maxV);
							gl.glVertex3d(pt[0], pt[1], z);
						}
						else if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
						else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));								
					}
				}
				gl.glEnd();
				if (pickMode) {
					//JOGLConfiguration.theLog.log(Level.INFO,"-");
					gl.glPopName();
				}
			}				
		}
		else
		for (int i = 0; i< sg.getNumFaces(); ++i)	{
			if (colorBind == PER_FACE) 		{					
				da = faceColors.item(i).toDoubleArray();
				if (colorLength == 3) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
				} 
			}
			if (pickMode) {
				//JOGLConfiguration.theLog.log(Level.INFO,"+G"+i+"\n");
				gl.glPushName( i);
			}
			if (normalBind == PER_FACE) {
				da = faceNormals.item(i).toDoubleArray();
				gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
			} 
			IntArray tf = sg.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
			gl.glBegin(GL.GL_POLYGON);
			for (int j = 0; j<tf.getLength(); ++j)	{
				int k = tf.getValueAt(j);
				if (normalBind == PER_VERTEX) {
					da = vertexNormals.item(k).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				} 
				if (colorBind == PER_VERTEX) {
					da = vertexColors.item(k).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
						} else if (colorLength == 4) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
						} 
				}
				if (texCoords != null)	 {
					da = texCoords.item(k).toDoubleArray();
					gl.glMultiTexCoord2d(GL.GL_TEXTURE0, da.getValueAt(0), da.getValueAt(1));
//					gl.glTexCoord2d(da.getValueAt(0), da.getValueAt(1));
				}
		        if (lightMapCoords != null) {
		            da = lightMapCoords.item(k).toDoubleArray();
		            gl.glMultiTexCoord2d(GL.GL_TEXTURE1, da.getValueAt(0), da.getValueAt(1));
		        }
				da = vertices.item(k).toDoubleArray();
				if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
			}
			gl.glEnd();
			if (pickMode) {
				//JOGLConfiguration.theLog.log(Level.INFO,"-");
				gl.glPopName();
			}
		}
		// pop to balance the glPushName(10000) above
		if (pickMode) gl.glPopName();
	}
	private static GLUT glut = new GLUT();
	static double[] correctionNDC = null;
	static {
		correctionNDC = Rn.identityMatrix(4);
		correctionNDC[10] = correctionNDC[11] = .5;
	}
	
	public static void drawLabels(LabelSet lb, JOGLRenderer jr)	{
		GL gl = jr.getCanvas().getGL();
		String[] labels = lb.getLabels();
		DataList positions = lb.getPositions();
		double[][] objectVerts, screenVerts;
		int bitmapFont = lb.getBitmapFont();
		
		objectVerts = positions.toDoubleArrayArray(null);
		screenVerts = new double[objectVerts.length][objectVerts[0].length];
		
		Graphics3D gc = jr.getContext();
		
		double[] objectToScreen = Rn.times(null, correctionNDC, gc.getObjectToScreen(jr.theViewer.getViewingComponent()));
//		System.out.println("object to Screen is \n"+Rn.matrixToString(objectToScreen));
		Rn.matrixTimesVector(screenVerts, objectToScreen, objectVerts);
		// It's important that the last coordinate is 0 when we transform to get screen coordinates:
		// don't want to pick up any translation
		double[] screenOffset = new double[4];
		System.arraycopy(lb.getNDCOffset(), 0, screenOffset,0,3);
		Rn.matrixTimesVector(screenOffset, Graphics3D.getNDCToScreen(jr.theViewer.getViewingComponent()), screenOffset);
		//System.out.println("Screen offset is "+Rn.toString(screenOffset));
		Rn.matrixTimesVector(screenVerts, objectToScreen, objectVerts);
		if (screenVerts[0].length == 4) Pn.dehomogenize(screenVerts, screenVerts);
		int np = objectVerts.length;

		// Store enabled state and disable lighting, texture mapping and the depth buffer
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
		for (int i = 0; i< 6; ++i) gl.glDisable(i + GL.GL_CLIP_PLANE0);

//		float[] cras = new float[4];
//		double[] dras = new double[4];
		for (int i = 0; i<np; ++i)	{
//			gl.glRasterPos3d(objectVerts[i][0], objectVerts[i][1], objectVerts[i][2]);
//			gl.glGetFloatv(GL.GL_CURRENT_RASTER_POSITION, cras);
//			for (int j = 0; j<4; ++j) dras[j] = cras[j];
			// TODO This is not available on ATI graphics card!      
			gl.glWindowPos3d(screenVerts[i][0]+screenOffset[0], screenVerts[i][1] +screenOffset[1], screenVerts[i][2]+screenOffset[2]);
			String label = (labels == null) ? Integer.toString(i) : labels[i];
			//bitmapFont = 2 + (i%6);
			glut.glutBitmapString(gl, bitmapFont, label);
		}

		gl.glPopAttrib();
	}

	double mat[] = new double[16];
	int lightCount =  GL.GL_LIGHT0;
	GL lightGL = null;
	static int maxLights = 8;
	OpenGLLightVisitor ogllv = new OpenGLLightVisitor();
	public void resetLights(GL globalGL, List lights)	{
		for (int i = 0; i<maxLights; ++i)	{
			globalGL.glLightf(GL.GL_LIGHT0+i, GL.GL_SPOT_CUTOFF, 180f);
			globalGL.glLightf(GL.GL_LIGHT0+i, GL.GL_SPOT_EXPONENT, (float) 0);
			globalGL.glLightf(GL.GL_LIGHT0+i, GL.GL_CONSTANT_ATTENUATION, 1.0f);
			globalGL.glLightf(GL.GL_LIGHT0+i, GL.GL_LINEAR_ATTENUATION, 0.0f);
			globalGL.glLightf(GL.GL_LIGHT0+i, GL.GL_QUADRATIC_ATTENUATION, 0.0f);
			globalGL.glDisable(GL.GL_LIGHT0+i);
		}
		for (int i = 0; i<lights.size(); ++i)	
			globalGL.glEnable(GL.GL_LIGHT0+i);
	}
	
	public void processLights(GL globalGL, List lights) {
		lightCount = GL.GL_LIGHT0;
		lightGL = globalGL;
		int n = lights.size();
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) lights.get(i);
			lp.getMatrix(mat);
//			JOGLConfiguration.theLog.log(Level.INFO,"Light matrix "+i+" is:\n"+Rn.matrixToString(mat));
//			JOGLConfiguration.theLog.log(Level.INFO,"Light"+i+": "+lp.toString());
			globalGL.glPushMatrix();
			globalGL.glMultTransposeMatrixd(mat);
			SceneGraphNode light = lp.getLastElement();
			light.accept(ogllv);
			globalGL.glPopMatrix();
			lightCount++;
			if (lightCount > GL.GL_LIGHT7)	{
				JOGLConfiguration.theLog.log(Level.WARNING,"Max. # lights exceeded");
			  	break;
			}
		}
	}
	
	private class OpenGLLightVisitor extends SceneGraphVisitor {
		public void visit(Light l) {
			wisit(l, lightGL, lightCount);
		}

		public void visit(DirectionalLight l) {
			wisit(l, lightGL, lightCount);
		}

		public void visit(PointLight l) {
			wisit(l, lightGL, lightCount);
		}

		public void visit(SpotLight l) {
			wisit(l, lightGL, lightCount);
		}
		
	}
	private static float[] zDirection = {0,0,1,(float)10E-10};
	private static float[] origin = {0,0,0,1};
	public static void wisit(Light dl, GL globalGL, int lightCount)	{
		  globalGL.glLightfv(lightCount, GL.GL_DIFFUSE, dl.getScaledColorAsFloat());
		  float f = (float) dl.getIntensity();
		  float[] specC = {f,f,f};
		  globalGL.glLightfv(lightCount, GL.GL_SPECULAR, specC);
		  globalGL.glLightfv(lightCount, GL.GL_AMBIENT, dl.getScaledColorAsFloat());	
	}
	
	public static void wisit(DirectionalLight dl, GL globalGL, int lightCount)		{
		  wisit( (Light) dl, globalGL, lightCount);
		  globalGL.glLightfv(lightCount, GL.GL_POSITION, zDirection);
	}
	
	public static  void wisit(PointLight dl, GL globalGL, int lightCount)		{
		  //gl.glLightfv(lightCount, GL.GL_AMBIENT, lightAmbient);
		  wisit((Light) dl, globalGL, lightCount);
		  globalGL.glLightfv(lightCount, GL.GL_POSITION, origin);
		  globalGL.glLightf(lightCount, GL.GL_CONSTANT_ATTENUATION, (float) dl.getFalloffA0());
		  globalGL.glLightf(lightCount, GL.GL_LINEAR_ATTENUATION, (float) dl.getFalloffA1());
		  globalGL.glLightf(lightCount, GL.GL_QUADRATIC_ATTENUATION, (float) dl.getFalloffA2());
	}
	
	public static void wisit(SpotLight dl, GL globalGL, int lightCount)		{
		  if (lightCount >= GL.GL_LIGHT7)	{
		  	JOGLConfiguration.theLog.log(Level.WARNING,"Max. # lights exceeded");
		  	return;
		  }
		  wisit((PointLight) dl, globalGL, lightCount);
		  globalGL.glLightf(lightCount, GL.GL_SPOT_CUTOFF, (float) ((180.0/Math.PI) * dl.getConeAngle()));
		  globalGL.glLightfv(lightCount, GL.GL_SPOT_DIRECTION, zDirection);
		  globalGL.glLightf(lightCount, GL.GL_SPOT_EXPONENT, (float) dl.getDistribution());
	}
	
	static double[] clipPlane = {0d, 0d, -1d, 0d};
	
	/**
	 * 
	 */
	public void processClippingPlanes(GL globalGL, List clipPlanes) {
		
		int clipBase = GL.GL_CLIP_PLANE0;
		// collect and process the lights
		// with a peer structure we don't do this but once, and then
		// use event listening to keep our list up-to-date
		// DEBUG: see what happens if we always reuse the light list
		int n = clipPlanes.size();
		//globalGL.glDisable(GL.GL_CLIP_PLANE0);
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) clipPlanes.get(i);
			//JOGLConfiguration.theLog.log(Level.INFO,"Light"+i+": "+lp.toString());
			SceneGraphNode cp = lp.getLastElement();
			if (!(cp instanceof ClippingPlane))	 JOGLConfiguration.theLog.log(Level.WARNING,"Invalid clipplane class "+cp.getClass().toString());
			else {
				double[] mat = lp.getMatrix(null);
				globalGL.glPushMatrix();
				globalGL.glMultTransposeMatrixd(mat);
				globalGL.glClipPlane(clipBase+i, clipPlane);
				globalGL.glEnable(clipBase+i);				
				globalGL.glPopMatrix();
			}
		}
	}
	
	public static void saveScreenShot(GLCanvas can, File file)	{
		saveScreenShot(can, can.getWidth(), can.getHeight(), file);
	}
	/**
	 * @param globalGL
	 * @param file
	 */
	public static void saveScreenShot(GLDrawable drawable, int width, int height, File file) {
			 
			// TODO figure out why channels = 4 doesn't work: transparency
			// getting written into fb even
		// though transparency disabled.
		int channels = 3;
		ByteBuffer pixelsRGBA = BufferUtils.newByteBuffer(width * height
				* channels);

		GL gl = drawable.getGL();

		if (drawable instanceof GLCanvas) {
			gl.glReadBuffer(GL.GL_BACK);
			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
		}

		gl.glReadPixels(0, // GLint x
				0, // GLint y
				width,// GLsizei width
				height, // GLsizei height
				channels == 3 ? GL.GL_RGB : GL.GL_RGBA, // GLenum format
				GL.GL_UNSIGNED_BYTE, // GLenum type
				pixelsRGBA); // GLvoid *pixels

		int[] pixelInts = new int[width * height];

		// Convert RGB bytes to ARGB ints with no transparency. Flip image
		// vertically by reading the
		// rows of pixels in the byte buffer in reverse - (0,0) is at bottom
		// left in OpenGL.

		int p = width * height * channels; // Points to first byte (red) in
											// each row.
		int q; // Index into ByteBuffer
		int i = 0; // Index into target int[]
		int w3 = width * channels; // Number of bytes in each row

		for (int row = 0; row < height; row++) {
			p -= w3;
			q = p;
			for (int col = 0; col < width; col++) {
				int iR = pixelsRGBA.get(q++);
				int iG = pixelsRGBA.get(q++);
				int iB = pixelsRGBA.get(q++);
				int iA = (channels == 3) ? 0xff : pixelsRGBA.get(q++);

				pixelInts[i++] = ((iA & 0x000000FF) << 24)
						| ((iR & 0x000000FF) << 16) | ((iG & 0x000000FF) << 8)
						| (iB & 0x000000FF);
			}

		}

		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);

		try {
			ImageIO.write(bufferedImage, "PNG", file);
			// ImageIO.write(bufferedImage, "TIF", file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JOGLConfiguration.theLog.log(Level.INFO, "Screenshot saved to "
				+ file.getName());
	}
	
// private static GLPbuffer getPbuffer(int width, int height, GLDrawable d) {
// GLPbuffer pbuffer = null;
// GLCapabilities caps = new GLCapabilities();
//        // doesn't seem to support anti-aliasing; just creates junk
////		caps.setSampleBuffers(true);
////		caps.setNumSamples(4);
//        //caps.setOffscreenRenderToTexture(true);
//		  caps.setDoubleBuffered(false); 
//		  JOGLConfiguration.getLogger().log(Level.INFO, "Caps is "+caps.toString());
//		  pbuffer = d.createOffscreenDrawable(caps, width, height);
//		  return pbuffer;
//	  }
//	  public static void renderOffscreen(int w, int h,final File file, final GLDrawable d)	{
//		  GLPbuffer pbuffer = null;
//		  File pbufferFile = null;
//		  pbufferFile = file;  
//		  final int width;
//		  if (w > 2048)	{
//			  JOGLConfiguration.getLogger().log(Level.WARNING,"Width being truncated to 2048");
//			  width = 2048;
//		  } else width = w;
//		  final int height;
//		  if (h > 2048)	{
//			  JOGLConfiguration.getLogger().log(Level.WARNING,"Height being truncated to 2048");
//			  height = 2048;
//		  } else height = w;
//		  pbuffer = getPbuffer(width, height, d);
//		  pbuffer.addGLEventListener(new  GLEventListener() {
//        		boolean done = false;
//			public void init(GLDrawable arg0) {
//	        	JOGLConfiguration.getLogger().log(Level.INFO,"PBuffer init");
//				
//			}
//
//			public void display(GLDrawable arg0) {
//				if (done) return;
//			   	JOGLConfiguration.getLogger().log(Level.INFO,"PBuffer display");
//			   	//JOGLRenderer renderer = new JOGLRenderer(me, pbuffer);
//			   	// have to set the rendering size since the jogl implementations of GLPbuffer
//			   	// don't implement getSize() (!!)
//			   	// we piggyback on the canvas's renderer.  To be safe, we need to put a lock around the
//			   	// following 3 lines of code.
//			   	renderer.setSize(width, height);
//			   	renderer.display(arg0);
//			   	renderer.setSize(d.getWidth(), d.getHeight());
//			   	JOGLRendererHelper.saveScreenShot(pbuffer,width, height, file);
//			   	done = true;
//			}
//
//			public void reshape(GLDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
//			   	JOGLConfiguration.getLogger().log(Level.INFO,"PBuffer reshape");
//			}
//
//			public void displayChanged(GLDrawable arg0, boolean arg1, boolean arg2) {
//			   	JOGLConfiguration.getLogger().log(Level.INFO,"PBuffer displayChanged");
//			}
//        });
//        System.err.println("Pbuffer created"); 
// 	    JOGLConfiguration.getLogger().log(Level.INFO,"Pbuffer is initialized: "+pbuffer.isInitialized());
//	  }

}
