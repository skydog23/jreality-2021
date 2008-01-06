package de.jreality.geometry;

import junit.framework.TestCase;

public class QuadMeshTest extends TestCase {

	public void testSetCoordinatesBug() {
		double[][][] grid1 = new double[][][]{
				{{0,0,0}, {1,0,0}, {2,0,0}},	
				{{0,1,0}, {1,1,0}, {2,1,0}},	
		};
		double[][][] grid2 = new double[][][]{
				{{0,0,0}, {1,0,0}, {2,0,0}},	
				{{0,1,0}, {1,1,0}, {2,1,0}},	
				{{0,2,0}, {1,2,0}, {2,2,0}},	
		};
		double[][][] grid3 = new double[][][]{
				{{0,0,0}, {1,0,0}},	
				{{0,1,0}, {1,1,0}},	
				{{0,2,0}, {1,2,0}},	
		};
		
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setVLineCount(2);
		qmf.setULineCount(3);
		qmf.setVertexCoordinates(grid1);
		qmf.update();
		qmf.setVLineCount(3);
		qmf.setULineCount(3);
		qmf.setVertexCoordinates(grid2);
		qmf.update();
		qmf.setVLineCount(3);
		qmf.setULineCount(2);
		qmf.setVertexCoordinates(grid3);
		qmf.update();
	}
}
