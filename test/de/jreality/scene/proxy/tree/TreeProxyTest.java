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


package de.jreality.scene.proxy.tree;

import java.util.Iterator;

import junit.framework.TestCase;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.proxy.ProxyFactory;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class TreeProxyTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(TreeProxyTest.class);
  }
  
  static class PrintFactory extends ProxyFactory {
    public Object getProxy() {
      //System.out.println("PrintFactory.getProxy()");
      return null;
    }
  }
  
  static class TreeDumper {
    StringBuffer indent=new StringBuffer(" ");
    void dumpTree(SceneTreeNode node) {
      boolean isValid=node.toPath().isValid();
      System.out.println(indent.substring(0, indent.length()-1)+"-"+node.getNode().getName()+"["+node.getNode().getClass().getName()+"] valid="+isValid);
      indent.append(" | ");
      if (!node.isLeaf())
        for (Iterator i = ((SceneTreeNode)node).getChildren().iterator(); i.hasNext(); )
          dumpTree((SceneTreeNode) i.next());
      indent.delete(indent.length()-3, indent.length());
    }
  }

  public void testTreeProxy() {
    SceneGraphComponent root = new SceneGraphComponent();
    SceneProxyTreeBuilder ttp = new SceneProxyTreeBuilder(root);
    ttp.setProxyTreeFactory(new ProxyTreeFactory());
    ttp.getProxyTreeFactory().setProxyFactory(new PrintFactory());
    ttp.setProxyConnector(new ProxyConnector());
    SceneTreeNode tn = ttp.createProxyTree();
    new TreeDumper().dumpTree(tn);
    System.out.println("++++++++++++++++++++++");
  }

  public void testTreeUpToDateProxy() {
    SceneGraphComponent root = new SceneGraphComponent();
    root.setName("root");
    SceneGraphComponent p1 = new SceneGraphComponent();
    p1.setName("p1");
    SceneGraphComponent p2 = new SceneGraphComponent();
    p2.setName("p2");
    SceneGraphComponent p3 = new SceneGraphComponent();
    p3.setName("p3");
    
    root.addChild(p1);
    root.addChild(p2);
    p2.setGeometry(new Sphere());
    
    UpToDateSceneProxyBuilder ttp = new UpToDateSceneProxyBuilder(root);
    
    TreeDumper td = new TreeDumper(); 
    ttp.setProxyTreeFactory(new ProxyTreeFactory());
    ttp.getProxyTreeFactory().setProxyFactory(new PrintFactory());
    ttp.setProxyConnector(new ProxyConnector());

    SceneTreeNode tn = ttp.createProxyTree();

    td.dumpTree(tn);
    System.out.println("created ++++++++++++++++++++++\n");

    root.addChild(p3);

    td.dumpTree(tn);
    System.out.println("added p3 to root ++++++++++++++++++++++\n");
    
    p1.addChild(p2);

    td.dumpTree(tn);
    System.out.println("added p2 to p1 ++++++++++++++++++++++\n");
    
    root.removeChild(p2);

    td.dumpTree(tn);
    System.out.println("removed p2 from root ++++++++++++++++++++++\n");

    p1.removeChild(p2);

    td.dumpTree(tn);
    System.out.println("removed p2 from p1 (now disposing entity?) ++++++++++++++++++++++\n");

    p1.addChild(p3);

    td.dumpTree(tn);
    System.out.println("added p3 to p1 ++++++++++++++++++++++\n");

    p2.addChild(p1);
    
    td.dumpTree(tn);
    System.out.println("added p1 to p2 (p2 not in tree) +++++++++++++\n");

    root.addChild(p2);

    td.dumpTree(tn);
    System.out.println("added p2 subtree ++++++++++++++++++++++\n");
  }
}
