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


package de.jreality.util;

import java.util.LinkedList;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;

/**
 * @author gunn
 *
 * This visitor traverses a scene graph searching for {@link Path the first path}from the given 
 * {@link SceneGraphComponent} and the given {@link SceneGraphNode}.
 * 
 * TODO: make this a collector, put methods in SceneGraphUtility (get all paths as well)
 * 
 * TODO: return list of ALL paths
 * 
 * make singleton 
 */
public class PathCollector extends SceneGraphVisitor {

    SceneGraphComponent root;
    SceneGraphPath currentPath = new SceneGraphPath();
    LinkedList collectedPaths = new LinkedList();
    Matcher matcher;
    
	  public PathCollector(Matcher matcher, SceneGraphComponent root)	{
	  	this.matcher=matcher;
      this.root=root;
	  }

	  /*
	   * Changed this method to reflect new policy on SceneGraphPath to allow
	   * SceneGraphNode to be final element of the path rather than segregated
	   * into separate field.  -gunn 4.6.4
	   */
	   public Object visit()	{
		  visit(root);
      return collectedPaths;
	  }
	  
	  public void visit(SceneGraphNode m) {
      currentPath.push(m);
			if (currentPath.getLength() > 0 && matcher.matches(currentPath)) {
			  collectedPaths.add(currentPath.clone());
      }
      currentPath.pop();
	  }
	
	  public void visit(SceneGraphComponent c) {
      super.visit(c);
  	  currentPath.push(c);
		  c.childrenAccept(this);
		  currentPath.pop();
	}
    
  public interface Matcher {
     boolean matches(SceneGraphPath p);
  };

}
