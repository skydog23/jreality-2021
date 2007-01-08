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

package de.jreality.sunflow;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Shader;
import org.sunflow.core.ParameterList.InterpolationType;
import org.sunflow.core.camera.PinholeLens;
import org.sunflow.core.light.DirectionalSpotlight;
import org.sunflow.core.light.SunSkyLight;
import org.sunflow.core.primitive.Background;
import org.sunflow.core.primitive.TriangleMesh;
import org.sunflow.core.shader.ConstantShader;
import org.sunflow.core.shader.DiffuseShader;
import org.sunflow.core.shader.GlassShader;
import org.sunflow.core.shader.IDShader;
import org.sunflow.core.shader.NormalShader;
import org.sunflow.core.shader.PrimIDShader;
import org.sunflow.core.shader.SimpleShader;
import org.sunflow.core.shader.UVShader;
import org.sunflow.core.shader.ViewCausticsShader;
import org.sunflow.core.shader.ViewGlobalPhotonsShader;
import org.sunflow.core.shader.ViewIrradianceShader;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.sunflow.core.light.DirectionalLight;
import de.jreality.sunflow.core.light.GlPointLight;
import de.jreality.sunflow.core.primitive.SkyBox;
import de.jreality.util.Rectangle3D;


public class SunflowRenderer extends SunflowAPI {

	IdentityHashMap<Object, String> geom2name = new IdentityHashMap<Object, String>();
	HashMap<String, Object> name2geom = new HashMap<String, Object>();

	private String POINT_SPHERE="point";
	private String LINE_CYLINDER="line";
	private SceneGraphPath bakingPath;
	private String bakingInstance;
	private RenderOptions options = new RenderOptions();
	private boolean ignoreSunLight;

	private class Visitor extends SceneGraphVisitor {

		SceneGraphPath path=new SceneGraphPath();
		EffectiveAppearance eapp;
		DefaultGeometryShader dgs;
		DefaultPolygonShader dps;
		Point3 sceneCenter;
		float sceneRadius;

		int lightID;
		private RenderingHintsShader rhs;
		private String shader;

		int appCount=0;
		private Matrix currentMatrix;

		int instanceCnt=0;

		Visitor(Point3 sceneCenter, float sceneRadius) {
			this.sceneCenter = sceneCenter;
			this.sceneRadius = sceneRadius;
		}
		
		@Override
		public void visit(SceneGraphComponent c) {
			if (!c.isVisible()) return;
			path.push(c);
			currentMatrix=new Matrix(path.getMatrix(null));
			eapp = EffectiveAppearance.create(path);
			shader = (String) eapp.getAttribute("sunflowShader", "default");

			dgs = ShaderUtility.createDefaultGeometryShader(eapp);
			rhs = ShaderUtility.createRenderingHintsShader(eapp);
			c.childrenAccept(this);
			path.pop();
		}

		@Override
		public void visit(IndexedFaceSet ifs) {
			visit((IndexedLineSet)ifs);
			if (ifs.getNumFaces() > 0 && dgs.getShowFaces()) {
				dps = (DefaultPolygonShader) dgs.getPolygonShader();
				applyShader(dps);
				float[] points = convert(ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(), 3, null);
				float[] normals = null;
				if (dps.getSmoothShading() && ifs.getVertexAttributes(Attribute.NORMALS) != null) {
					normals = convert(ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(), 3, null);
					parameter("normals", "vector", "vertex", normals);
				} else {
					// sunflow calculates the face normal from the triangle points...
				}
				DataList tex = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
				float[] texCoords = null;
				if (tex != null) {
					Matrix texMat = null;
					// this is needed for sunflow build-in shaders:
					//MatrixBuilder.euclidean().scale(1,-1,1).getMatrix();
					//texMat.multiplyOnRight(tex2d.getTextureMatrix());
					texCoords = convert(tex.toDoubleArrayArray(), 2, texMat);
				}
				int[] faces = convert(ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray());
				parameter("triangles", faces);
				parameter("points", "point", "vertex", points);
				if (texCoords != null) {
					parameter("uvs", "texcoord", "vertex", texCoords);				
				}
				geometry(getName(ifs), new TriangleMesh());
				parameter("transform", currentMatrix);

				String geomName = getName(ifs);
				String instanceName = geomName + ".instance"+instanceCnt++;
				if (bakingPath == null) {
					parameter("shaders", "default-shader" + appCount);
				} else {
					//System.out.println("path is "+path.getLastElement().getName());
					//System.out.println("bakingPath is "+bakingPath.getLastElement().getName());
					if (path.isEqual(bakingPath)) {
						bakingInstance = instanceName;
						//parameter("shaders", "constantWhite");
						parameter("shaders", "ambientOcclusion");
					} else {
						parameter("shaders", "constantWhite");
					}
				}
				instance(instanceName, geomName);
			}
		}

		@Override
		public void visit(IndexedLineSet indexedLineSet) {
			visit((PointSet)indexedLineSet);
			DefaultLineShader ls = (DefaultLineShader) dgs.getLineShader();
			if (dgs.getShowLines() && ls.getTubeDraw() && indexedLineSet.getNumEdges() > 0 && indexedLineSet.getNumPoints() > 0) {
				dps = (DefaultPolygonShader) ls.getPolygonShader();
				double r = ls.getTubeRadius();
				DoubleArrayArray pts = indexedLineSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
				IntArrayArray lines = indexedLineSet.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
				DataList radiiAttributes = indexedLineSet.getEdgeAttributes(Attribute.RADII);
				DoubleArray radii = radiiAttributes != null ? radiiAttributes.toDoubleArray() : null;
				double[] zAxis = pts.getLengthAt(0) == 3 ? new double[]{0,0,-1} : new double[]{0,0,-1,1};
				DataList colorAttributes = indexedLineSet.getEdgeAttributes(Attribute.COLORS);
				boolean lineColors = colorAttributes != null;
				DoubleArrayArray colors = lineColors ? colorAttributes.toDoubleArrayArray() : null;
				if (!lineColors) {
					applyShader(dps);
				}
				for (int i=0; i<lines.getLength(); i++) {
					double radius = radii != null ? radii.getValueAt(i) : r;
					if (lineColors) {
						Appearance app = new Appearance("fake app");
						EffectiveAppearance ea = eapp.create(app);
						dgs = ShaderUtility.createDefaultGeometryShader(ea);
						dps = (DefaultPolygonShader) ((DefaultLineShader) dgs.getLineShader()).getPolygonShader();
						double[] vc = colors.getValueAt(i).toDoubleArray(null);
						java.awt.Color vcc = new java.awt.Color((float) vc[0], (float) vc[1], (float) vc[2]);
						app.setAttribute("lineShader.polygonShader.diffuseColor", vcc);
						if (vc.length == 4) {
							app.setAttribute("lineShader.polygonShader.transparency", dps.getTransparency()*vc[3]);
						}
						applyShader(dps);
					}
					for (int j=0; j<lines.getLengthAt(i)-1; j++) {
						double[] p1 = pts.getValueAt(lines.getValueAt(i, j)).toDoubleArray(null);
						double[] p2 = pts.getValueAt(lines.getValueAt(i, j+1)).toDoubleArray(null);
						double[] seg = Rn.subtract(null, p2, p1);
						double[] center = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
						double len=Rn.euclideanNorm(seg);
						Matrix m = Matrix.times(currentMatrix, MatrixBuilder.euclidean()
								.translate(
										center[0],
										center[1],
										center[2]
								).rotateFromTo(zAxis, seg)
								.scale(radius, radius, len/2).getMatrix());
						parameter("transform", m);
						if (bakingPath == null) {
							parameter("shaders", "default-shader" + appCount);
						} else {
							parameter("shaders", "constantWhite");
						}
						instance(LINE_CYLINDER + ".instance"+instanceCnt++, LINE_CYLINDER);
					}
				}
			}
		}

		@Override
		public void visit(PointSet pointSet) {
			DefaultPointShader ps = (DefaultPointShader) dgs.getPointShader();
			if (dgs.getShowPoints() && ps.getSpheresDraw() && pointSet.getNumPoints() > 0) {
				dps = (DefaultPolygonShader) ps.getPolygonShader();
				double r = ps.getPointRadius();
				DoubleArrayArray pts = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
				DataList radiiAttributes = pointSet.getVertexAttributes(Attribute.RADII);
				DoubleArray radii = radiiAttributes != null ? radiiAttributes.toDoubleArray() : null;
				DataList colorAttributes = pointSet.getVertexAttributes(Attribute.COLORS);
				boolean vertexColors = colorAttributes != null;
				DoubleArrayArray colors = vertexColors ? colorAttributes.toDoubleArrayArray() : null;
				if (!vertexColors) {
					applyShader(dps);
				}
				for (int i=0; i<pts.getLength(); i++) {
					if (vertexColors) {
						Appearance app = new Appearance("fake app");
						EffectiveAppearance ea = eapp.create(app);
						dgs = ShaderUtility.createDefaultGeometryShader(ea);
						dps = (DefaultPolygonShader) ((DefaultPointShader) dgs.getPointShader()).getPolygonShader();
						double[] vc = colors.getValueAt(i).toDoubleArray(null);
						java.awt.Color vcc = new java.awt.Color((float) vc[0], (float) vc[1], (float) vc[2]);
						app.setAttribute("pointShader.polygonShader.diffuseColor", vcc);
						if (vc.length == 4) {
							app.setAttribute("pointShader.polygonShader.transparency", dps.getTransparency()*vc[3]);
						}
						applyShader(dps);
					}
					double w = pts.getLengthAt(i) == 3 ? 1 : pts.getValueAt(i, 3);
					if (w != 0) {
						Matrix m = Matrix.times(currentMatrix, MatrixBuilder.euclidean().translate(
								pts.getValueAt(i, 0)/w,
								pts.getValueAt(i, 1)/w,
								pts.getValueAt(i, 2)/w
						).scale(radii != null ? radii.getValueAt(i) : r).getMatrix());
						parameter("transform", m);
						if (bakingPath == null) {
							parameter("shaders", "default-shader" + appCount);
						} else {
							parameter("shaders", "constantWhite");
						}
						instance(POINT_SPHERE + ".instance"+instanceCnt++, POINT_SPHERE);
					}
				}
			}
		}

		@Override
		public void visit(de.jreality.scene.DirectionalLight l) {
			if (!l.isAmbientFake() || ("sun light".equals(l.getName()) && !ignoreSunLight)) {
				double[] d = currentMatrix.multiplyVector(new double[]{0,0,-1,0});
				Vector3 dir = new Vector3((float)d[0], (float)d[1], (float)d[2]);
				Point3 src = new Point3(
						sceneCenter.x - sceneRadius * dir.x,
						sceneCenter.y - sceneRadius * dir.y,
						sceneCenter.z - sceneRadius * dir.z
				);
				System.out.println("source at "+src);
				System.out.println("direction "+dir);
				System.out.println("radius "+sceneRadius);
				
				parameter("source", src);
				parameter("radius", sceneRadius);
				parameter("dir", dir);
				DirectionalSpotlight sun = new DirectionalSpotlight();
				java.awt.Color c = l.getColor();
				float i = (float)l.getIntensity() *(float)Math.PI;
				Color col = new Color(c.getRed()/255f*i, c.getGreen()/255f*i, c.getBlue()/255f*i);
				parameter("radiance", col);
				light("directionalLight"+lightID++, sun);
			}
		}

		@Override
		public void visit(de.jreality.scene.PointLight l) {
			if (!l.isAmbientFake()) {
				double[] point = currentMatrix.multiplyVector(new double[]{0,0,0,1});
				parameterPoint("center", point);
				GlPointLight light = new GlPointLight();
				java.awt.Color c = l.getColor();
				float i = (float)l.getIntensity() *(float)Math.PI;
				Color col = new Color(c.getRed()/255f*i, c.getGreen()/255f*i, c.getBlue()/255f*i);
				parameter("power", col);
				parameter("fallOffA0", l.getFalloffA0());
				parameter("fallOffA1", l.getFalloffA1());
				parameter("fallOffA2", l.getFalloffA2());
				light("pointLight"+lightID++, light);
			}
		}

		@Override
		public void visit(Sphere s) {
			geometry(getName(s), new org.sunflow.core.primitive.Sphere());
		}

		@Override
		public void visit(Cylinder c) {
			geometry(getName(c), new de.jreality.sunflow.core.primitive.Cylinder());
		}

		private void applyShader(DefaultPolygonShader ps) {
			appCount++;
			if ("default".equals(shader)) {
				shader("default-shader"+appCount, new de.jreality.sunflow.core.shader.DefaultPolygonShader(ps, rhs));
			} else if ("glass".equals(shader)) {
				System.out.println("applying glass shader");
				parameter("color", ps.getDiffuseColor());
				shader("default-shader"+appCount, new GlassShader());
			}
		}		
	}


	public int[] convert(IntArrayArray faces) {
		int triCnt=0;
		for (int i=0; i<faces.getLength(); i++) {
			triCnt+=(faces.getLengthAt(i)-2);
		}
		int[] tris = new int[triCnt*3];
		int ind=0;
		for (int i=0; i<faces.getLength(); i++) {
			IntArray face = faces.getValueAt(i);
			for (int k=0; k<face.getLength()-2; k++) {
				tris[ind++]=face.getValueAt(0);
				tris[ind++]=face.getValueAt(k+1);
				tris[ind++]=face.getValueAt(k+2);
			}
		}
		return tris;
	}

	public float[] convert(DoubleArrayArray array, int slotLen, Matrix matrix) {
		float[] ret = new float[array.getLength()*slotLen];
		double[] tmp = new double[4];
		tmp[3]=1;
		int ind=0;
		for (int i=0; i<array.getLength(); i++) {
			for (int j=0; j<slotLen; j++) {
				tmp[j]=array.getValueAt(i, j);
			}
			if (matrix != null) tmp = matrix.multiplyVector(tmp);
			for (int j=0; j<slotLen; j++) {
				ret[ind++]=(float) tmp[j];
			}
		}
		return ret;
	}

	public void render(
			SceneGraphComponent sceneRoot,
			SceneGraphPath cameraPath,
			SceneGraphPath bakingPath,
			Display display,
			int width,
			int height
	) {
		this.bakingPath = bakingPath;
		render(sceneRoot, cameraPath, display, width, height);
		bakingPath = null;
	}

	public void render(
			SceneGraphComponent sceneRoot,
			SceneGraphPath cameraPath,
			Display display,
			int width,
			int height
	) {
		shader("constantWhite", new ConstantShader());
		shader("ambientOcclusion", new DiffuseShader());


		String shaderOverride = options.getShaderOverride();
		Shader overrideShader = null;
		if ("viewCaustics".equals(shaderOverride)) {
			overrideShader = new ViewCausticsShader();
		} else if ("viewGlobalPhotons".equals(shaderOverride)) {
			overrideShader = new ViewGlobalPhotonsShader();
		} else if ("viewIrradiance".equals(shaderOverride)) {
			overrideShader = new ViewIrradianceShader();
		} else if ("uv".equals(shaderOverride)) {
			overrideShader = new UVShader();
		} else if ("id".equals(shaderOverride)) {
			overrideShader = new IDShader();
		} else if ("simple".equals(shaderOverride)) {
			overrideShader = new SimpleShader();
		} else if ("primID".equals(shaderOverride)) {
			overrideShader = new PrimIDShader();
		} else if ("normal".equals(shaderOverride)) {
			overrideShader = new NormalShader();
		}
		if (overrideShader != null) {
			shader("overrideShader", overrideShader);
			shaderOverride("overrideShader", false);
		} else {
			// skybox or background color
			Appearance rootApp = sceneRoot.getAppearance();
			Geometry rootGeom = sceneRoot.getGeometry();
			if (options.isUseSunSkyLight() && rootGeom instanceof PerezSky) {
				ignoreSunLight = true;
				PerezSky perezSky = (PerezSky) rootGeom;
				ParameterList pl = new ParameterList();
				double[] dir = perezSky.getSunDirection();
				pl.addVectors(
						"up",
						InterpolationType.NONE,
						new float[] {0,1,0}
				);
				pl.addVectors(
						"sundir",
						InterpolationType.NONE,
						new float[] {(float)dir[0], (float)dir[2], (float)dir[1]}
				);
				SunSkyLight skyLight = new SunSkyLight();
				skyLight.update(pl, this);
				skyLight.init("sunSky", this);
			} else if(rootApp != null) {
				if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,
						CommonAttributes.SKY_BOX, rootApp)) {
					CubeMap cm = (CubeMap) AttributeEntityUtility
					.createAttributeEntity(CubeMap.class,
							CommonAttributes.SKY_BOX, rootApp, true);
					SkyBox skyBox = new SkyBox(cm);
					parameter("center", new Vector3(1, 0, 0));
					parameter("up", new Vector3(0, -1, 0));
					skyBox.init("skyBox", this);
				} else {

					try{  
						java.awt.Color backColor = (java.awt.Color) rootApp.getAttribute(CommonAttributes.BACKGROUND_COLOR);
						if (backColor != null) {
							parameter("color", backColor);
							shader("background.shader", new ConstantShader());
							geometry("background", new Background());
							parameter("shaders", "background.shader");
							instance("background.instance", "background");
						}
					}catch(ClassCastException e){System.err.println("\nonly uniform background colors supported yet");}
				}
			}
		}

		// add texture path
		try {
			File tmpF = File.createTempFile("foo", ".png");
			addTextureSearchPath(tmpF.getParentFile().getAbsolutePath());
			if (!tmpF.delete()) tmpF.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// init default primitives
		geometry(POINT_SPHERE, new org.sunflow.core.primitive.Sphere());
		geometry(LINE_CYLINDER, new de.jreality.sunflow.core.primitive.Cylinder());

		Rectangle3D sceneBounds = GeometryUtility.calculateBoundingBox(sceneRoot);
		double[] c = sceneBounds.getCenter();
		Point3 sceneCenter = new Point3((float)c[0], (float)c[1], (float)c[2]);
		double[] e = sceneBounds.getExtent();
		float sceneRadius = (float)Math.sqrt(e[0]*e[0] + e[1]*e[1] + e[2]*e[2])/2;
		// visit
		new Visitor(sceneCenter, sceneRadius).visit(sceneRoot);

		// camera
		float aspect = width/(float)height;
		parameter("aspect",aspect);
		Camera camera = (Camera) cameraPath.getLastElement();
		Matrix m = new Matrix(cameraPath.getMatrix(null));
		parameter("transform",m);
		double fov = camera.getFieldOfView();
		if (width>height) {
			fov = Math.atan(((double)width)/((double)height)*Math.tan(fov/360*Math.PI))/Math.PI*360;
		}
		parameter("fov", fov);
		String name = getUniqueName("camera");
		camera(name, new PinholeLens());
		parameter("camera", name);

		// sunflow rendering

		parameter("sampler", options.isProgessiveRender() ? "ipr" : "bucket");
		if (bakingInstance != null) {
			parameter("baking.instance", bakingInstance);
		}
		parameter("resolutionX", width);
		parameter("resolutionY", height);
		parameter("threads.lowPriority", options.isThreadsLowPriority());
		parameter("aa.min", options.getAaMin());
		parameter("aa.max", options.getAaMax());
		parameter("depths.diffuse", options.getDepthsDiffuse());
		parameter("depths.reflection", options.getDepthsReflection());
		parameter("depths.refraction", options.getDepthsRefraction());

		int causticPhotons = options.getCausticsEmit();
		if (causticPhotons > 0) {
			parameter("caustics","kd");
			parameter("caustics.emit", options.getCausticsEmit());
			parameter("caustics.gather", options.getCausticsGather());
			parameter("caustics.radius", options.getCausticsRadius());
			parameter("caustics.filter", options.getCausticsFilter());
		}

		String giEngine = options.getGiEngine();
		if ("ambocc".equals(giEngine)) {
			float ambient = (float)options.getAmbientOcclusionBright();
			int ambientOcclusionSamples = options.getAmbientOcclusionSamples();
			parameter("gi.engine", "ambocc");
			parameter("gi.ambocc.bright", new Color(ambient, ambient, ambient));
			parameter("gi.ambocc.dark", Color.BLACK);
			parameter("gi.ambocc.samples", ambientOcclusionSamples);
			parameter("gi.ambocc.maxdist", 100f);
		} else if ("igi".equals(giEngine)) {
			parameter("gi.engine", "igi");
		} else if ("fake".equals(giEngine)) {
			parameter("gi.engine", "fake");
		} else if ("path".equals(giEngine)) {
			parameter("gi.engine", "path");
		} else if ("irr-cache".equals(giEngine)) {
			parameter("gi.engine", "irr-cache");
			parameter("gi.irr-cache.gmap","kd");
		}

		options(SunflowAPI.DEFAULT_OPTIONS);
		render(SunflowAPI.DEFAULT_OPTIONS, display);
	}

	public String getName(Geometry geom) {
		String prefix=geom.getName();
		return getName(prefix, geom);
	}

	private String getName(String prefix, Object geom) {
		String ret;
		if (geom2name.containsKey(geom)) ret = geom2name.get(geom);
		else {
			if (!name2geom.containsKey(prefix)) {
				geom2name.put(geom, prefix);
				ret = prefix;
			} else {
				int counter = 1;
				String name;
				do {
					name = String.format("%s_%d", prefix, counter);
					counter++;
				} while (name2geom.containsKey(name));
				name2geom.put(name, geom);
				geom2name.put(geom, name);
				ret = name;
			}
		}
		return ret;
	}

	public void parameter(String string, java.awt.Color c) {
		parameter(string, new Color(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f));
	}

	public void parameter(String name, Matrix m) {
		parameter(name, new Matrix4(
				(float) m.getEntry(0, 0),
				(float) m.getEntry(0, 1),
				(float) m.getEntry(0, 2),
				(float) m.getEntry(0, 3),
				(float) m.getEntry(1, 0),
				(float) m.getEntry(1, 1),
				(float) m.getEntry(1, 2),
				(float) m.getEntry(1, 3),
				(float) m.getEntry(2, 0),
				(float) m.getEntry(2, 1),
				(float) m.getEntry(2, 2),
				(float) m.getEntry(2, 3),
				(float) m.getEntry(3, 0),
				(float) m.getEntry(3, 1),
				(float) m.getEntry(3, 2),
				(float) m.getEntry(3, 3)
		));
	}

	public void parameterPoint(String name, double[] column) {
		parameter(name, new Point3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameterVector(String name, double[] column) {
		parameter(name, new Vector3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameter(String name, double val) {
		parameter(name, (float) val);
	}

	public RenderOptions getOptions() {
		return options;
	}

	public void setOptions(RenderOptions options) {
		this.options = options;
	}

}