package de.jreality.ui.treeview;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.proxy.tree.ProxyTreeFactory;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.ShaderUtility;

public class SceneTreeModel extends AbstractTreeModel {

  private UpToDateSceneProxyBuilder builder;
  
  WeakHashMap entities = new WeakHashMap();
  WeakHashMap parents = new WeakHashMap();

  public SceneTreeModel(SceneGraphComponent root) {
    super(null);
    setSceneRoot(root);
  }

  void dispose() {
    builder.dispose();
    builder = null;
  }
  
  void setSceneRoot(SceneGraphComponent comp) {
    if (builder != null) {
      throw new IllegalStateException("twice called");
    }
    if (comp == null) return;
    builder = new UpToDateSceneProxyBuilder(comp);
    builder.setProxyTreeFactory(new ProxyTreeFactory() {
      public SceneTreeNode createProxyTreeNode(SceneGraphNode n) {
        return new SceneTreeNode(n) {
          public int addChild(SceneTreeNode child) {
            int ret = super.addChild(child);
            fireNodesAdded(this, new Object[]{child});
            return ret;
          }
          protected int removeChild(SceneTreeNode prevChild) {
            int ret = super.removeChild(prevChild);
            fireNodesRemoved(this, new int[]{ret}, new Object[]{prevChild});
            return ret;
          }
        };
      }
    });
    super.root = builder.createProxyTree();
  }

  public Object getChild(Object parent, int index) {
    if (parent instanceof SceneTreeNode) {
      SceneTreeNode sn = (SceneTreeNode) parent;
      if (sn.getNode() instanceof SceneGraphComponent) {
        if (index < sn.getChildren().size()) return sn.getChildren().get(index);
        int newInd = index-sn.getChildren().size();
        SceneGraphComponent comp = (SceneGraphComponent) sn.getNode();
        Tool t = (Tool) comp.getTools().get(newInd);
        return TreeTool.getInstance(sn, t);
      }
      if (!(sn.getNode() instanceof Appearance))
        return sn.getChildren().get(index);
    }
    Object[] childEntities = (Object[]) entities.get(parent);
    return childEntities[index];
  }

  public int getChildCount(Object parent) {
    if (parent instanceof TreeTool) return 0;
    if (parent instanceof SceneTreeNode) {
      SceneTreeNode sn = (SceneTreeNode)parent;
      if ((sn.getNode() instanceof Appearance)) {
        Object[] ents = (Object[]) entities.get(sn);
        if (ents == null) {
          Object o1 = ShaderUtility.createDefaultGeometryShader((Appearance) sn.getNode(), false);
          Object o2 = null;
          if (AttributeEntityUtility.hasAttributeEntity(RootAppearance.class, "", (Appearance)sn.getNode()))
            o2 = ShaderUtility.createRootAppearance((Appearance) sn.getNode());
          ents = new Object[o2 == null ? 1 : 2];
          ents[0] = o1;
          if (o2 != null) ents[1] = o2;
          entities.put(sn, ents);
          for (int i = 0; i < ents.length; i++)
            parents.put(ents[i], sn);
        }
        return ents.length;
      } else {
        int ret = sn.getChildren().size(); 
        if (sn.getNode() instanceof SceneGraphComponent)
          ret += ((SceneGraphComponent)sn.getNode()).getTools().size();
        return ret;
      }
    } else {
      // entity
      Object[] ents = (Object[]) entities.get(parent);
      if (ents == null) {
        BeanInfo bi=null;
        try {
          bi = Introspector.getBeanInfo(parent.getClass());
        } catch (IntrospectionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        PropertyDescriptor[] pd=bi.getPropertyDescriptors();
        List childEntities = new LinkedList();
        for (int i = 0; i < pd.length; i++) {
          if (!AttributeEntity.class.isAssignableFrom(pd[i].getPropertyType())) continue;
          try {
            AttributeEntity ae = (AttributeEntity) pd[i].getReadMethod().invoke(parent, null);
            if (ae != null) {
              childEntities.add(ae);
              parents.put(ae, parent);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        ents = childEntities.toArray();
        entities.put(parent, ents);
      }
      return ents.length;
    }
  }

  public Object getParent(Object o) {
    if (o instanceof SceneTreeNode && !(((SceneTreeNode)o).getNode() instanceof Appearance) )
      return ((SceneTreeNode)o).getParent();
    if (o instanceof TreeTool) return ((TreeTool)o).getTreeNode();
    else return parents.get(o);
  }
  
  public static class TreeTool {
    
    static WeakHashMap map = new WeakHashMap();
    
    private final SceneTreeNode treeNode;
    private final Tool tool;

    static TreeTool getInstance(SceneTreeNode n, Tool t) {
      HashMap m = (HashMap) map.get(n);
      if (m == null) {
        m = new HashMap();
        map.put(n, m);
      }
      HashMap m2 = (HashMap) m.get(t);
      if (m2 == null) {
        m2 = new HashMap();
        m2.put(t, new TreeTool(n, t));
      }
      return (TreeTool) m2.get(t);
    }
    
    private TreeTool(SceneTreeNode n, Tool t) {
      this.treeNode = n;
      this.tool = t;
    }

    public Tool getTool() {
      return tool;
    }

    public SceneTreeNode getTreeNode() {
      return treeNode;
    }
  }
  
}