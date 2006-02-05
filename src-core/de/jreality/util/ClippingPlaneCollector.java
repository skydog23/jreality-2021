/*
 * Created on Nov 4, 2004
 *
 */
package de.jreality.util;

import java.util.Vector;

import de.jreality.scene.*;

/**
 * @author gunn
 *
 *@deprecated use {@link de.jreality.util.SceneGraphUtility.collectClippingPlanes(node)}
  */
public class ClippingPlaneCollector extends SceneGraphVisitor {

	SceneGraphComponent sgc;
	SceneGraphPath currentPath;
	Vector lightList;
	public ClippingPlaneCollector(SceneGraphComponent b)	{
	  	sgc = b;
	  	lightList = new Vector();
	}

	 public Object visit()	{
	   	currentPath = new SceneGraphPath();
	   	lightList.clear();
	   	if (sgc == null) return lightList;
	   	visit(sgc);
	   	return lightList;
	 }

	public void visit(ClippingPlane l) {
		SceneGraphPath foundOne ;
		foundOne = (SceneGraphPath) currentPath.clone();
		foundOne.push(l);
		lightList.add(foundOne);
	}
	
	public void visit(SceneGraphComponent c) {
		currentPath.push(c);
		c.childrenAccept(this);
		currentPath.pop();
	}
}
