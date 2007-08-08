/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.jogl.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.WeakHashMap;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

/**
 * it is assumed that the shader source code stayes FIXED!
 * 
 * @author Steffen Weissmann
 *
 */
public class GlslPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

	private static final int PER_VERTEX = 0;
	private static final int PER_FACE = 1;
	private static final int PER_PART = 2;
	GlslProgram program;

	Texture2D normalTex, diffuseTex;
	
	CubeMap environmentMap;
	private VertexShader vertexShader;
	private boolean smoothShading;
	private int frontBack=DefaultPolygonShader.FRONT_AND_BACK;
	RenderingHintsShader rhsShader=new RenderingHintsShader();
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		super.setFromEffectiveAppearance(eap, name);
		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SMOOTH_SHADING), CommonAttributes.SMOOTH_SHADING_DEFAULT);
		if (GlslProgram.hasGlslProgram(eap, name)) {
			// dummy to write glsl values like "lightingEnabled"
			Appearance app = new Appearance();
			EffectiveAppearance eap2 = eap.create(app);
			program = new GlslProgram(app, eap2, name);
		} else program = null;
		if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "normalMap"), eap)) {
			normalTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "normalMap"), eap);
		} else normalTex = null;
		if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, CommonAttributes.TEXTURE_2D), eap)) {
			diffuseTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, CommonAttributes.TEXTURE_2D), eap);
		} else diffuseTex = null;
		if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name, "reflectionMap"), eap)) {
			environmentMap = (CubeMap) AttributeEntityUtility.createAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name, "reflectionMap"), eap);
		} else environmentMap = null;
		vertexShader = (VertexShader) ShaderLookup.getShaderAttr(eap, name, CommonAttributes.VERTEX_SHADER);
		rhsShader.setFromEffectiveAppearance(eap, "");
	}

	public void render(JOGLRenderingState jrs) {
		rhsShader.render(jrs);
		JOGLRenderer jr = jrs.getRenderer();
		GL gl = jr.getGL();
		if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
		else gl.glShadeModel(GL.GL_FLAT);
		jrs.smoothShading = smoothShading;

		vertexShader.setFrontBack(frontBack);
		vertexShader.render(jrs);
	   
		if (diffuseTex != null) {
			gl.glActiveTexture(GL.GL_TEXTURE0);
			Texture2DLoaderJOGL.render(jr.getGL(), diffuseTex);
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		if (normalTex != null) {
			gl.glActiveTexture(GL.GL_TEXTURE1);
			Texture2DLoaderJOGL.render(jr.getGL(), normalTex);
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		if (environmentMap != null) {
			gl.glActiveTexture(GL.GL_TEXTURE2);
			Texture2DLoaderJOGL.render(jr, environmentMap);
			gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
		}
		if (program != null) {
			if (program.getSource().getUniformParameter("lightingEnabled") != null) {
				program.setUniform("lightingEnabled", rhsShader.isLightingEnabled());
			}
			if (program.getSource().getUniformParameter("transparency") != null) {
				program.setUniform("transparency", rhsShader.isTransparencyEnabled() ? vertexShader.getDiffuseColorAsFloat()[3] : 0f);
			}
			GlslLoader.render(program, jr);
		}
		Geometry g = jrs.getCurrentGeometry();
		if (g != null)	{
			if (g instanceof Sphere || g instanceof Cylinder)	{	
				int i = 3;
				if (false) {//jr.debugGL)	{
					double lod = jr.renderingState.levelOfDetail;
					i = JOGLSphereHelper.getResolutionLevel(jr.getContext().getObjectToNDC(), lod);
				}
				int dlist;
				if (g instanceof Sphere) dlist = jr.renderingState.getSphereDisplayLists(i);
				else 			 dlist = jr.renderingState.getCylinderDisplayLists(i);
				if (jr.isPickMode()) jr.getGL().glPushName(JOGLPickAction.GEOMETRY_BASE);
				jr.getGL().glCallList(dlist);
				if (jr.isPickMode()) jr.getGL().glPopName();
			}
			else if ( g instanceof IndexedFaceSet)	{
				drawFaces(jr, (IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);
//				JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);			
			}
		}
	}

	public void postRender(JOGLRenderingState jrs) {
		JOGLRenderer jr = jrs.getRenderer();
		GL gl = jr.getGL();
		if (program != null)  GlslLoader.postRender(program, jr);
		if (diffuseTex != null) {
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		if (normalTex != null) {
			gl.glActiveTexture(GL.GL_TEXTURE1);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		if (environmentMap != null) {
			gl.glActiveTexture(GL.GL_TEXTURE2);
			gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
			gl.glDisable(GL.GL_TEXTURE_GEN_S);
			gl.glDisable(GL.GL_TEXTURE_GEN_T);
			gl.glDisable(GL.GL_TEXTURE_GEN_R);
		}
	}

	public void setFrontBack(int f) {
		frontBack=f;
	}

	public void setProgram(GlslProgram program) {
		this.program = program;
	}

	public static void drawFaces(JOGLRenderer jr, IndexedFaceSet sg, boolean smooth, double alpha) {
		if (sg.getNumFaces() == 0)
			return;
		GL gl = jr.getGL();
		boolean pickMode = jr.isPickMode();

		int colorBind = -1, normalBind, colorLength = 3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg
		.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		DataList lightMapCoords = sg.getVertexAttributes(Attribute
				.attributeForName("lightmap coordinates"));
		// JOGLConfiguration.theLog.log(Level.INFO,"Vertex normals are:
		// "+((vertexNormals != null) ? vertexNormals.size() : 0));
		// JOGLConfiguration.theLog.log(Level.INFO,"alpha value is "+alpha);

		// vertex color has priority over face color
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		if (vertexColors != null && smooth) {
			colorBind = PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} else if (faceColors != null && colorBind != PER_VERTEX) {
			colorBind = PER_FACE;
			colorLength = GeometryUtility.getVectorLength(faceColors);
		} else
			colorBind = PER_PART;
		// JOGLConfiguration.theLog.log(Level.INFO,"Color binding is
		// "+colorBind);
		if (colorBind != PER_PART) {
			if (jr.renderingState.frontBack != DefaultPolygonShader.FRONT_AND_BACK) {
				gl.glEnable(GL.GL_COLOR_MATERIAL);
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK,
						GL.GL_DIFFUSE);
				jr.renderingState.frontBack = DefaultPolygonShader.FRONT_AND_BACK;
			}
		}
		if (vertexNormals != null && smooth) {
			normalBind = PER_VERTEX;
		} else if (faceNormals != null && (vertexNormals == null || !smooth)) {
			normalBind = PER_FACE;
		} else
			normalBind = PER_PART;

		renderFaces(sg, alpha, gl, pickMode, colorBind, normalBind, colorLength, vertices, vertexNormals, faceNormals, vertexColors, faceColors, texCoords, lightMapCoords, vertexLength, smooth);
	}

	private static void renderFaces(IndexedFaceSet sg, double alpha, GL gl, boolean pickMode, int colorBind, int normalBind, int colorLength, DataList vertices, DataList vertexNormals, DataList faceNormals, DataList vertexColors, DataList faceColors, DataList texCoords, DataList lightMapCoords, int vertexLength, boolean smooth) {
		Attribute TANGENTS=Attribute.attributeForName("TANGENTS");

		DataList tanCoords = sg.getVertexAttributes(TANGENTS);

		boolean faceN = normalBind == PER_FACE;
		
		boolean faceC = colorBind == PER_FACE;
		
		// what does this flag mean??? it is always true.
		boolean renderInlined = (normalBind == PER_VERTEX || faceN) && (colorBind == PER_VERTEX || colorBind == PER_PART || faceC);

		if (renderInlined) {

			gl = new DebugGL(gl);

			// count indices
			int triagCnt=0;
			IntArrayArray faces = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray();
			int numFaces = faces.getLength();
			for (int i = 0; i < numFaces; i++) {
				triagCnt+=(faces.getLengthAt(i)-2);
			}

			boolean hasColors = vertexColors != null || faceColors != null;

			IntBuffer indexBuffer = null;
			boolean inlineI = false; // = normalBind == PER_FACE || hasColors;
			if (inlineI) {
				indexBuffer = BufferCache.index(sg, triagCnt);
			}
			DoubleBuffer vertexBuffer = null;
			double[] tmpV = new double[vertexLength];
			boolean inlineV = true;
			if (inlineV) {
				vertexBuffer = BufferCache.vertex(sg, triagCnt, vertexLength);
			}

			double[] tmpTex = new double[2];
			boolean inlineTex = texCoords != null;
			DoubleBuffer texBuffer = null;
			if (inlineTex) {
				texBuffer = BufferCache.texCoord(sg, triagCnt);
			}

			double[] tmpTan = new double[4];
			boolean inlineTan = tanCoords != null;
			DoubleBuffer tanBuffer = null;
			if (inlineTan) {
				tanBuffer = BufferCache.tangent(sg, triagCnt, 4);
			}

			double[] tmpN = new double[3];
			boolean inlineN = true;
			DoubleBuffer normalBuffer = BufferCache.normal(sg, triagCnt);

			double[] tmpC = new double[colorLength];
			boolean inlineC = hasColors;
			DoubleBuffer colorBuffer = null;
			if (inlineC) {
				colorBuffer = BufferCache.color(sg, triagCnt, colorLength);
			}

			if (!upToDate(sg, smooth)) {

				DoubleArray da;

				DoubleArrayArray verts = vertices.toDoubleArrayArray();
				DoubleArrayArray tc = inlineTex ? texCoords.toDoubleArrayArray() : null;
				DoubleArrayArray t = inlineTan ? tanCoords.toDoubleArrayArray() : null;
				DoubleArrayArray norms = faceN ? faceNormals.toDoubleArrayArray() : vertexNormals.toDoubleArrayArray();
				DoubleArrayArray cols = inlineC ? (faceC ? faceColors.toDoubleArrayArray() : vertexColors.toDoubleArrayArray()) : null;

				for (int i = 0; i < numFaces; i++) {
					IntArray face = faces.getValueAt(i);
					for (int j = 0; j < face.getLength()-2; j++) {
						final int i1 = face.getValueAt(0);
						final int i2 = face.getValueAt(j+1);
						final int i3 = face.getValueAt(j+2);
						if (inlineI) {
							indexBuffer.put(i1);
							indexBuffer.put(i2);
							indexBuffer.put(i3);
						}
						if (inlineV) {
							da = verts.getValueAt(i1);
							da.toDoubleArray(tmpV);
							try {
								vertexBuffer.put(tmpV);
							} catch (Exception e) {
								System.out.println(vertexBuffer);
								System.out.println("triags="+triagCnt);
							}
							da = verts.getValueAt(i2);
							da.toDoubleArray(tmpV);
							vertexBuffer.put(tmpV);
							da = verts.getValueAt(i3);
							da.toDoubleArray(tmpV);
							vertexBuffer.put(tmpV);
						}
						if (inlineTex) {
							da = tc.getValueAt(i1);
							da.toDoubleArray(tmpTex);
							texBuffer.put(tmpTex);
							da = tc.getValueAt(i2);
							da.toDoubleArray(tmpTex);
							texBuffer.put(tmpTex);
							da = tc.getValueAt(i3);
							da.toDoubleArray(tmpTex);
							texBuffer.put(tmpTex);
						}
						if (inlineTan) {
							da = t.getValueAt(i1);
							da.toDoubleArray(tmpTan);
							tanBuffer.put(tmpTan);
							da = t.getValueAt(i2);
							da.toDoubleArray(tmpTan);
							tanBuffer.put(tmpTan);
							da = t.getValueAt(i3);
							da.toDoubleArray(tmpTan);
							tanBuffer.put(tmpTan);
						}
						if (inlineN) {
							da = norms.getValueAt(faceN ? i : i1);
							da.toDoubleArray(tmpN);
							normalBuffer.put(tmpN);
							if (!faceN) {
								da = norms.getValueAt(i2);
								da.toDoubleArray(tmpN);
							}
							normalBuffer.put(tmpN);
							if (!faceN) {
								da = norms.getValueAt(i3);
								da.toDoubleArray(tmpN);
							}
							normalBuffer.put(tmpN);
						}
						if (inlineC) {
							da = cols.getValueAt(faceC ? i : i1);
							da.toDoubleArray(tmpC);
							colorBuffer.put(tmpC);
							if (!faceC) {
								da = cols.getValueAt(i2);
								da.toDoubleArray(tmpC);
							}
							colorBuffer.put(tmpC);
							if (!faceC) {
								da = cols.getValueAt(i3);
								da.toDoubleArray(tmpC);
							}
							colorBuffer.put(tmpC);
						}
					}
				}
			}
			vertexBuffer.rewind();
			normalBuffer.rewind();

			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL.GL_NORMAL_ARRAY);

			gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, vertexBuffer);
			gl.glNormalPointer(GL.GL_DOUBLE, 0, normalBuffer);
			if (hasColors) {
				gl.glEnableClientState(GL.GL_COLOR_ARRAY);
				colorBuffer.rewind();
				gl.glColorPointer(colorLength, GL.GL_DOUBLE, 0, colorBuffer);
			}
			if (texCoords != null) {
				gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
				texBuffer.rewind();
				gl.glTexCoordPointer(2, GL.GL_DOUBLE, 0, texBuffer);
			}
			int TANGENT_ID=9;
			if (tanCoords != null) {
				tanBuffer.rewind();
				gl.glVertexAttribPointer(TANGENT_ID, 4, GL.GL_DOUBLE, true, 0, tanBuffer);
				gl.glEnableVertexAttribArray(TANGENT_ID);
			}
			if (inlineI) {
				indexBuffer.rewind();
				gl.glDrawElements(GL.GL_TRIANGLES, indexBuffer.remaining(), GL.GL_UNSIGNED_INT, indexBuffer);
			}
			else gl.glDrawArrays(GL.GL_TRIANGLES, 0, triagCnt*3);

			gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
			if (texCoords != null) {
				gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			}
			if (hasColors) {
				gl.glDisableClientState(GL.GL_COLOR_ARRAY);
			}
			if (tanCoords != null) {
				gl.glDisableVertexAttribArray(TANGENT_ID);
			}

		} else {
			System.out.println("GlslPolygonShader inlined: ??");
		}
	}

	private static HashMap<IndexedFaceSet, Boolean> upToDateIFS = new HashMap<IndexedFaceSet, Boolean>();

	private static boolean upToDate(final IndexedFaceSet sg, boolean smooth) {
		if (upToDateIFS.get(sg) == Boolean.valueOf(smooth)) return true;
		else {
			upToDateIFS.put(sg, Boolean.valueOf(smooth));
			sg.addGeometryListener(new GeometryListener() {
				public void geometryChanged(GeometryEvent ev) {
					if (!ev.getChangedVertexAttributes().isEmpty() || !ev.getChangedFaceAttributes().isEmpty()) {
						upToDateIFS.remove(sg);
						sg.removeGeometryListener(this);
					}
				}
			});
			return false;
		}
	}


	private static final class BufferCache {

		static WeakHashMap<IndexedFaceSet, ByteBuffer> vertexBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();
		static WeakHashMap<IndexedFaceSet, ByteBuffer> texCoordBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();
		static WeakHashMap<IndexedFaceSet, ByteBuffer> tangentBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();
		static WeakHashMap<IndexedFaceSet, ByteBuffer> normalBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();
		static WeakHashMap<IndexedFaceSet, ByteBuffer> colorBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();
		static WeakHashMap<IndexedFaceSet, ByteBuffer> indexBuffers = new WeakHashMap<IndexedFaceSet, ByteBuffer>();

		private BufferCache() {}

		static DoubleBuffer vertex(IndexedFaceSet ifs, int numTris, int vertexLen) {
			return get(ifs, vertexBuffers, numTris*3*vertexLen*8).asDoubleBuffer();
		}

		static DoubleBuffer texCoord(IndexedFaceSet ifs, int numTris) {
			return get(ifs, texCoordBuffers, numTris*3*2*8).asDoubleBuffer();
		}

		static DoubleBuffer tangent(IndexedFaceSet ifs, int numTris, int tangentLen) {
			return get(ifs, tangentBuffers, numTris*3*tangentLen*8).asDoubleBuffer();
		}

		static DoubleBuffer normal(IndexedFaceSet ifs, int numTris) {
			return get(ifs, normalBuffers, numTris*3*3*8).asDoubleBuffer();
		}

		static DoubleBuffer color(IndexedFaceSet ifs, int numTris, int colorLen) {
			return get(ifs, colorBuffers, numTris*3*colorLen*8).asDoubleBuffer();
		}

		static IntBuffer index(IndexedFaceSet ifs, int numTris) {
			return get(ifs, indexBuffers, numTris*3*4).asIntBuffer();
		}

		private static ByteBuffer get(IndexedFaceSet ifs, WeakHashMap<IndexedFaceSet, ByteBuffer> cache, int capacity) {
			ByteBuffer bb = cache.get(ifs);
			if (bb == null || bb.capacity() < capacity) {
				bb = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
				cache.put(ifs, bb);
			}
			bb.position(0).limit(capacity);
			return bb;
		}

	};

}
