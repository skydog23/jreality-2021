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


package de.jreality.jogl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Level;

import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.GLPbuffer;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.SceneGraphUtility;
public class GLJPanelViewer extends Viewer {
	GLJPanel panel;
	GLPbuffer sharedPBuffer;
	boolean opaque = false;
	
	public GLJPanelViewer() {
		this(null, null);
	}

	public GLJPanelViewer(SceneGraphPath camPath, SceneGraphComponent root) {
		setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		initializeFrom(root, camPath);	
		panel.setOpaque(opaque);
	}

	// override these methods as subclass to draw beneath (above) the jReality scene
	public void paintBefore(Graphics g)	{
//		getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,255,0,128));
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setColor(Color.blue);
//		g2.fillRect(0, 50, 50, 50);		
	}
	
	public void paintAfter(Graphics g)	{
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setColor(Color.pink);
//		g2.fillRect(0, 0, 50, 50);
	}
	
	  @Override
	  protected void initializeFrom(SceneGraphComponent r, SceneGraphPath p)	{
		setSceneRoot(r);
		setCameraPath(p);
		GLCapabilities caps = new GLCapabilities();
		caps.setAlphaBits(8);
		caps.setStereo(JOGLConfiguration.quadBufferedStereo);
		caps.setDoubleBuffered(true);
		GLCapabilitiesChooser chooser = new MultisampleChooser();
		if (JOGLConfiguration.multiSample)	{
			caps.setSampleBuffers(true);
			caps.setNumSamples(4);
			caps.setStereo(JOGLConfiguration.quadBufferedStereo);
		} else {
			chooser = new DefaultGLCapabilitiesChooser();
		}
		if (JOGLConfiguration.sharedContexts && firstOne == null) 
			setupSharedContext(caps, chooser);
		panel = new GLJPanel(caps, chooser, firstOne) {

			@Override
			protected void paintComponent(Graphics arg0) {
				paintBefore(arg0);
				super.paintComponent(arg0);
				paintAfter(arg0);
			}
			
		};
		drawable = panel;
        JOGLConfiguration.getLogger().log(Level.INFO, "Caps is "+caps.toString());
        drawable.addGLEventListener(this);
 		if (JOGLConfiguration.quadBufferedStereo) setStereoType(HARDWARE_BUFFER_STEREO);
// 		panel.updateUI();
	}

	  // have to use a pbuffer to start with since panel has no context until
	  // it's visible.
	  private void setupSharedContext(GLCapabilities caps, GLCapabilitiesChooser chooser) {
		if (sharedPBuffer == null)
			sharedPBuffer = GLDrawableFactory.getFactory().createGLPbuffer(caps, chooser, 1,1, null);
		firstOne = sharedPBuffer.getContext();
	  }

	public boolean isOpaque() {
		return opaque;
	}

	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
		panel.setOpaque(opaque);
	}
	
}
