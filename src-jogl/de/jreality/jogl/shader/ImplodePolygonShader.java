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

import java.util.logging.Level;

import javax.media.opengl.GL;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.LoggingSystem;

/**
 * @author gunn
 *
 */
public class ImplodePolygonShader extends DefaultPolygonShader {
    double implodeFactor;
	private int implodeDL = -1;

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		implodeFactor = eap.getAttribute(ShaderUtility.nameSpace(name, "implodeFactor"), implodeFactor);
     }
    
	public double getImplodeFactor() {
		return implodeFactor;
	}
	public boolean providesProxyGeometry() {		
		//if (implodeFactor == 0.0) return false;
		return true;
	}
	public int proxyGeometryFor(JOGLRenderingState jrs)	{
		final Geometry original = jrs.getCurrentGeometry();
		final JOGLRenderer jr = jrs.getRenderer();
		final int sig = jrs.getCurrentSignature();
		final boolean useDisplayLists = jrs.isUseDisplayLists();
		if (!(original instanceof IndexedFaceSet)) return -1;
		if (implodeDL != -1) return implodeDL;
		GL gl = jr.getGL();
		JOGLConfiguration.theLog.log(Level.FINE,this+"Providing proxy geometry "+implodeFactor);
		IndexedFaceSet ifs =  IndexedFaceSetUtility.implode((IndexedFaceSet) original, implodeFactor);
		double alpha = vertexShader == null ? 1.0 : vertexShader.getDiffuseColorAsFloat()[3];
		if (useDisplayLists) {
			implodeDL = gl.glGenLists(1);
			gl.glNewList(implodeDL, GL.GL_COMPILE);
		}
		//if (jr.isPickMode())	gl.glPushName(JOGLPickAction.GEOMETRY_BASE);
    JOGLRendererHelper.drawFaces(jr, ifs,  isSmoothShading(), alpha);
		//if (jr.isPickMode())	gl.glPopName();
		if (useDisplayLists) gl.glEndList();
		return implodeDL;
	}

	public void flushCachedState(JOGLRenderer jr) {
		super.flushCachedState(jr);
		LoggingSystem.getLogger(this).fine("ImplodePolygonShader: Flushing display lists "+implodeDL+" : "+dListProxy);
		if (implodeDL != -1) { jr.getGL().glDeleteLists(implodeDL, 1);  implodeDL = -1; }
	}
}
