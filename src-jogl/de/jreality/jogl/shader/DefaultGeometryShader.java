/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * @author Charles Gunn
 *
 */
public class DefaultGeometryShader  implements Shader {
		
		boolean faceDraw = true, 
		 	vertexDraw = false, 
		 	edgeDraw = true;
		// these should be more general shaders, but since we only have one type of each ...
		public PolygonShader polygonShader;
		public LineShader lineShader;
		public PointShader pointShader;
		/**
		 * 
		 */
		public DefaultGeometryShader() {
			super();
//			polygonShader = new DefaultPolygonShader();
//			lineShader = new DefaultLineShader();
//			pointShader = new DefaultPointShader();
		}
			
	public static DefaultGeometryShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		DefaultGeometryShader dgs = new DefaultGeometryShader();
		dgs.setFromEffectiveAppearance(eap, name);
		return dgs;
	}
	
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		String geomShaderName = "";		// at a later date this may be a field;
		vertexDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.VERTEX_DRAW), CommonAttributes.VERTEX_DRAW_DEFAULT);
		edgeDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.EDGE_DRAW), CommonAttributes.EDGE_DRAW_DEFAULT );
		faceDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(faceDraw) {
	        polygonShader =ShaderLookup.getPolygonShaderAttr(eap, geomShaderName, CommonAttributes.POLYGON_SHADER);
	    } else {
	    		polygonShader = null;
	    }
	    if(edgeDraw) {
	    		lineShader =ShaderLookup.getLineShaderAttr(eap, geomShaderName, CommonAttributes.LINE_SHADER);
	    } else {
	        	lineShader = null;
	    }
	       
	    if(vertexDraw) {
	        pointShader=ShaderLookup.getPointShaderAttr(eap, geomShaderName, CommonAttributes.POINT_SHADER);
	    } else {
	        pointShader=null;
	    }
	}

		/**
		 * @return
		 */
		public boolean isEdgeDraw() {
			return edgeDraw;
		}

		/**
		 * @return
		 */
		public boolean isFaceDraw() {
			return faceDraw;
		}

		/**
		 * @return
		 */
		public boolean isVertexDraw() {
			return vertexDraw;
		}

		/**
		 * @return
		 */
		public Shader getLineShader() {
			return lineShader;
		}

		/**
		 * @return
		 */
		public Shader getPointShader() {
			return pointShader;
		}

		/**
		 * @return
		 */
		public Shader getPolygonShader() {
			return polygonShader;
		}

		/* (non-Javadoc)
		 * @see de.jreality.jogl.shader.Shader#render(de.jreality.jogl.JOGLRendererNew)
		 */
		public void render(JOGLRenderer jr) {
		}

		public void postRender(JOGLRenderer jr) {
		}

}
