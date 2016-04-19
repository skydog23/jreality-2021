package de.jreality.ui;

import javax.swing.JFrame;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

public class Display1Test {

	
	
	public static void main(String[] args) {
		GLCapabilitiesChooser capChooser = new DefaultGLCapabilitiesChooser();
		GLCapabilities caps = new GLCapabilities(GLProfile.get("GL2"));
		System.out.println("using caps: " + caps);
		GLCanvas canvas = new GLCanvas(caps, capChooser, null);
		
		JFrame f = new JFrame("Display 1 Test");
		f.setSize(1024, 600);
		f.add(canvas);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
}
