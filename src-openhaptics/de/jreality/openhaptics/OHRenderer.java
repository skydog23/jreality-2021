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


package de.jreality.openhaptics;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import de.jreality.jogl.ConstructPeerGraphVisitor;
import de.jreality.jogl.JOGLRenderer;
import de.varylab.openhaptics.HD;
import de.varylab.openhaptics.HDErrorInfo;
import de.varylab.openhaptics.HL;
import de.varylab.openhaptics.HLU;
import de.varylab.openhaptics.HLerror;

public class OHRenderer  extends JOGLRenderer {

	public OHRenderer(OHViewer viewer) {
		super(viewer);
		renderingState = new OHRenderingState(this);
	}
	
	
	@Override
	public OHViewer getViewer() {
		return (OHViewer)super.getViewer();
	}


	protected void renderPeerRoot() {

		if(devicePresent){
			hlFitWorkspace();

			for(OHRawDevice dev : getViewer().getRawDevices())
				dev.checkDevice();
			
			((OHRenderingState)renderingState).isHapticRendering = true;
			HL.hlBeginFrame();
			checkHLError();
	
			// Use OpenGL commands to create geometry.
			thePeerRoot.render();
	
			// End the haptic frame.
			((OHRenderingState)renderingState).isHapticRendering = false;
			HL.hlEndFrame();
			checkHLError();
	
			drawCursor(globalGL); // TODO allow free geometry
		}
		
		
		thePeerRoot.render();
	}

	public void init(GL gl) {
		ConstructPeerGraphVisitor.setPeerClass(OHPeerComponent.class);

		super.init(gl);
		initHL();
		
		
		for(OHRawDevice dev : getViewer().getRawDevices())
			dev.start();
	}

	public final static int CURSOR_SIZE_PIXELS = 20;
	static double gCursorScale;

	int hHD;
	long hHLRC;

//	int hlShapeId;
	void initHL() {
		hHD = HD.hdInitDevice(HD.HD_DEFAULT_DEVICE);
		devicePresent = true;
		if (checkHDError()) {
			devicePresent = false;
		}

		if(!devicePresent) return;

		
		hHLRC = HL.hlCreateContext(hHD);
		checkHDError();
		
		HL.hlMakeCurrent(hHLRC);
		checkHLError();

		// Enable optimization of the viewing parameters when rendering
		// geometry for OpenHaptics.
		HL.hlEnable(HL.HL_HAPTIC_CAMERA_VIEW);
		checkHLError();

		// Generate id for the shape.
//		hlShapeId = HL.hlGenShapes(1);
		checkHLError();

		HL.hlTouchableFace(HL.HL_FRONT);
		checkHLError();
	}

	static void checkHLError() {
		HLerror error;
		while(HL.HL_ERROR(error = HL.hlGetError())){
			System.err.println(error);
		}
	}

	static boolean checkHDError() {
		return checkError(HD.hdGetError());
	}

	static boolean checkError(HDErrorInfo error) {
		if(HD.HD_DEVICE_ERROR(error)) {
			System.out.println(error);
			return true;
		}
		return false;
	}

	int gCursorDisplayList = 0;
	GLU glu = new GLU();

	boolean devicePresent = false;
	
	
	public boolean isDevicePresent() {
		return devicePresent;
	}


	void drawCursor(GL gl) {
		double kCursorRadius = 0.5;
		double kCursorHeight = 1.5;
		int kCursorTess = 15;
		double proxyxform[] = new double[16];

		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_ENABLE_BIT);
		gl.glPushMatrix();
		
		if (gCursorDisplayList == 0) {
			gCursorDisplayList = gl.glGenLists(1);
			gl.glNewList(gCursorDisplayList, GL.GL_COMPILE);
			GLUquadric qobj = glu.gluNewQuadric();

			glu.gluCylinder(qobj, 0.0, kCursorRadius, kCursorHeight,
					kCursorTess, kCursorTess);
			gl.glTranslated(0.0, 0.0, kCursorHeight);
			glu.gluCylinder(qobj, kCursorRadius, 0.0, kCursorHeight / 5.0,
					kCursorTess, kCursorTess);

			glu.gluDeleteQuadric(qobj);
			gl.glEndList();
		}
		
		if(devicePresent){
			// Get the proxy transform in world coordinates.
			HL.hlGetDoublev(HL.HL_PROXY_TRANSFORM, proxyxform, 0);
			checkHLError();
			gl.glMultMatrixd(proxyxform, 0);
//			System.out.println(proxyxform[12]+", "+ proxyxform[13]+", "+proxyxform[14]);
		}
		else{
		}

		// Apply the local cursor scale factor.
//		gl.glScaled(gCursorScale, gCursorScale, gCursorScale);

		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColor3f(0.0f, 0.5f, 1.0f);
		gl.glCallList(gCursorDisplayList);
		gl.glPopMatrix();
		gl.glPopAttrib();
	}

	private void hlFitWorkspace() {
		if(!devicePresent) return;

		double modelview[] = new double[16];
		double projection[] = new double[16];
		int viewport[] = new int[4];

		globalGL.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelview, 0);
		globalGL.glGetDoublev(GL.GL_PROJECTION_MATRIX, projection, 0);
		globalGL.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

		HL.hlMatrixMode(HL.HL_TOUCHWORKSPACE);
		HL.hlLoadIdentity();
		checkHLError();

		// Fit haptic workspace to view volume.
		
		HLU.hluFitWorkspaceBox(modelview, 0, getViewer().getP0(), 0, getViewer().getP1(), 0);
//		HLU.hluFitWorkspace(projection, 0);
		checkHLError();

		// Compute cursor scale.
		gCursorScale = HLU.hluScreenToModelScale(modelview, 0, projection, 0, viewport, 0);
		gCursorScale *= CURSOR_SIZE_PIXELS;
	}
}
