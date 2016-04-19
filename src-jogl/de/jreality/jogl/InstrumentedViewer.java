package de.jreality.jogl;

import com.jogamp.opengl.GLEventListener;

public interface InstrumentedViewer extends GLEventListener {
	public void installOverlay();
	public void uninstallOverlay();
	public double getClockRate();	// frame rate full application
	public double getFrameRate();	// frame rate backend only
	public int getPolygonCount();
	public void addGLEventListener(GLEventListener e); // need to be able to pass display() on
}