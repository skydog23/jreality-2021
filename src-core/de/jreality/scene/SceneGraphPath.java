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


package de.jreality.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.jreality.math.Rn;

/**
 * A SceneGraphPath represents a directed connection in the scene graph. Technically it is a list of 
 * SceneGraphComponents. It may also include, optionally, a {@link #SceneGraphNode}
 *  contained in the final {@link #SceneGraphComponent}.
 * This allows addressing the sub-nodes contained as fields in the SceneGraphComponent 
 * (such as lights, camera, geometry, appearance). 
 * But it is not required that the path ends in such a SceneGraphNode; it can also
 * end in a SceneGraphComponent.
 * <p>
 * There are methods for pushing and popping elements onto the path, useful for instances
 * of {@link SceneGraphVisitor}.
 * <p>
 * There are methods for ascertaining the matrix transformation associated to the path
 * (by multiplying the instances of {@link Transformation} occurring on the path.
 * <b>Note:</b> This class takes no care of the elements being inserted. The method isValid()
 * gives information if this path exists in the scenegraph
 * @author Tim Hoffman, Charles Gunn, Steffen Weissman
 *
 */
public class SceneGraphPath implements Cloneable {

  protected LinkedList<SceneGraphNode> path = new LinkedList<SceneGraphNode>();			// a list of SceneGraphComponents

  public static SceneGraphPath fromList(List<SceneGraphNode> list) {
    SceneGraphPath path=new SceneGraphPath();
    path.path.addAll(list);
    return path;
  }

	public String toString() {
    if(path.isEmpty()) return "<< empty path >>";
    StringBuffer sb = new StringBuffer();
    for(int ix=0, n=path.size(); ix < n; ix++)
    {
      sb.append(((SceneGraphNode)path.get(ix)).getName()).append(" : ");
    }
    sb.setLength(sb.length()-3);
    return sb.toString();
  }
  
  public Object clone() {
    SceneGraphPath path=new SceneGraphPath();
    path.path.addAll(this.path);
    return path;
  }
  
  public List<SceneGraphNode> toList() {
    return new ArrayList<SceneGraphNode>(path);
  }

  // TODO write own Iterator classes...
  
  public ListIterator iterator() {
    return Collections.unmodifiableList(path).listIterator();
  }
  public ListIterator iterator(int start) {
    return Collections.unmodifiableList(path).listIterator(start);
  }
  
  /**
   * 
   * @param start how many knodes from the end of the path should we leave out?
   * i.e.: p.reverseIterator(p.getLength()) gives the same result as p.reverseIterator()
   * @return a reverse iterator from the given position
   */
  public Iterator reverseIterator(int start) {
    final ListIterator iter = iterator(start);
    return new Iterator() {

      public void remove() {
          iter.remove();
      }

      public boolean hasNext() {
          return iter.hasPrevious();
      }

      public Object next() {
          return iter.previous();
      }
    };
  }

  public Iterator reverseIterator() {
    return reverseIterator(path.size());
  }

  /**
	 * Gives the length of the path
	 * @return int
	 */
	public int getLength() {
		return path.size();
	}
  
  public final void push(final SceneGraphNode c) {
    path.add(c);
  }
  /**
   * lets this path unchanged
   * @return a new path that is equal to this path after calling path.push(c)
   */
  public final SceneGraphPath pushNew(final SceneGraphNode c) {
    SceneGraphPath ret = SceneGraphPath.fromList(path);
    ret.path.add(c);
    return ret;
  }

  public final void pop() {
    path.removeLast();
  }
  /**
   * lets this path unchanged
   * @return a new path that is equal to this path after calling path.pop()
   */
  public final SceneGraphPath popNew() {
    SceneGraphPath ret = SceneGraphPath.fromList(path);
    ret.path.removeLast();
    return ret;
  }
    
  public SceneGraphNode getFirstElement() {
    return (SceneGraphNode) path.getFirst();
  }

  public SceneGraphNode getLastElement() {
    return (SceneGraphNode) path.getLast();
  }

  public SceneGraphComponent getLastComponent() {
    if (!(path.getLast() instanceof SceneGraphComponent)) return (SceneGraphComponent) path.get(path.size()-2);
    return (SceneGraphComponent) path.getLast();
  }

	public void clear()	{
		path.clear();
	}
	
  /**
   * checks if the path is really an existing path in the scenegraph.
   * 
   * @return true if the path exists
   */
	public boolean isValid()
  {
    if (path.size()==0) return true;
    Iterator i = path.iterator();
    SceneGraphNode parent = (SceneGraphNode) i.next();
    try {
  		for ( ; i.hasNext(); )	{
  			SceneGraphNode child = (SceneGraphNode) i.next();
  			if(!((SceneGraphComponent)parent).isDirectAncestor(child)) return false;
        if (i.hasNext()) parent = child;
  		}
    } catch (ClassCastException cce) {
      // this happens if a non-component node is somwhere IN the path
      return false;
    }
		return true;
	}

	public boolean equals(Object p) {
		if (p instanceof SceneGraphPath)
    		return isEqual((SceneGraphPath) p);
		return false;
	}
	
	public boolean isEqual(SceneGraphPath anotherPath)
	{
		if (anotherPath == null || path.size() != anotherPath.getLength())	return false;

		for (int i=0; i<path.size(); ++i)	{
				if (!path.get(i).equals(anotherPath.path.get(i))) return false;
		}
		return true;
	}

  public boolean startsWith(SceneGraphPath potentialPrefix) {
      if (getLength() < potentialPrefix.getLength()) return false;
      Iterator i1 = iterator();
      Iterator i2 = potentialPrefix.iterator();
      for (; i2.hasNext();) {
          if (i1.next() != i2.next()) return false;
      }
      return true;
  }
  /*** matrix calculations ***/
	
	public double[] getMatrix(double[] aMatrix)
	{
		return getMatrix(aMatrix, 0, path.size()-1);
	}

  public double[] getMatrix(double[] aMatrix, int begin)
  {
    return getMatrix(aMatrix, begin, path.size()-1);
  }

  public double[] getInverseMatrix(double[] invMatrix)
	{
		return getInverseMatrix(invMatrix, 0, path.size()-1);
	}

  public double[] getInverseMatrix(double[] invMatrix, int begin)
  {
    return getInverseMatrix(invMatrix, begin, path.size()-1);
  }

  public double[] getMatrix(double[] aMatrix, int begin, int end)
	{
		final double[] myMatrix;
		if (aMatrix == null) myMatrix = new double[16];
		else myMatrix = aMatrix;
		Rn.setIdentityMatrix(myMatrix);

    for (ListIterator it = path.listIterator(begin); it.nextIndex() <= end; )  
    {
      Object currObj = it.next();
      if (currObj instanceof SceneGraphComponent) {
        SceneGraphComponent currComp = (SceneGraphComponent)currObj;
        Transformation tt = currComp.getTransformation();
        if (tt == null) continue;
        Rn.times(myMatrix, myMatrix, tt.getMatrix());         
      }
    }
		return myMatrix;
	}
	
	public double[] getInverseMatrix(double[] aMatrix, int begin, int end)
	{
		double[] mat = getMatrix(aMatrix, begin, end);
		return Rn.inverse(mat, mat);
	}

}
