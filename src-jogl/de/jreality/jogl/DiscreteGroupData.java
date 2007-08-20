package de.jreality.jogl;

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;

public class DiscreteGroupData extends Geometry {
	public DiscreteGroupData(String name) {
		super(name);
	}
	public double[][] matrixList = {Rn.identityMatrix(4)};
	public double minDistance = -1, maxDistance = -1;
	public double ndcFudgeFactor = 1.2;
	public boolean clipToCamera = false;
	public boolean componentDisplayLists = false;
	public int signature;
	public int count;
	public int delay = 150;
	public SceneGraphComponent child;
}
