/*
 * Created on Nov 13, 2022
 *
 */
package de.jreality.tutorial.util;

public class PrintClassPath {

	public static void printClassPath() {
		String cp = ((String)System.getProperty("java.class.path")).replace(':', '\n'); //split(":");
		System.err.println("cp = "+cp);
	}
}
