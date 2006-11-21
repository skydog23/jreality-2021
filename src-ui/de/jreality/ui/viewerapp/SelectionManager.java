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


package de.jreality.ui.viewerapp;

import static de.jreality.ui.viewerapp.SelectionEvent.DEFAULT_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.ENTITY_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.NO_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.TOOL_SELECTION;

import java.util.Vector;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphPathObserver;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.util.Rectangle3D;


/**
 * Manages selections of scene graph nodes within a scene graph displayed by a viewer.
 * 
 * @author msommer
 */
public class SelectionManager implements TransformationListener {
  
  private SceneGraphPath defaultSelection;
  private SceneGraphPath selection;
  private SceneGraphPathObserver selectionObserver;

  private Vector<SelectionListener> listeners;
  private SelectionListener smListener = null;
  private Tool tool = null;               //currently selected tool
  private AttributeEntity entity = null;  //currently selected attribute entity
  private boolean nothingSelected = true;  //true if default selection is selected by manager, e.g. setSelection(null)
  
  private boolean renderSelection = false;  //default
  private Viewer viewer = null;
  private SceneGraphComponent auxiliaryRoot;
  private SceneGraphComponent selectionKit;
  
  
  public SelectionManager(SceneGraphPath defaultSelection) {
    if (defaultSelection == null)
      throw new IllegalArgumentException("Default selection is null!");
    
    //listen to changes of the selection's transformation matrix
    selectionObserver = new SceneGraphPathObserver();
    selectionObserver.addTransformationListener(this);
    selectionObserver.setPath(selection);
    
    listeners = new Vector<SelectionListener>();
    
    //set default selection
    setDefaultSelection(defaultSelection);
    setSelection(null);
  }
  
  
  final Navigator.SelectionListener navigatorListener = new Navigator.SelectionListener(){
      public void selectionChanged(Navigator.SelectionEvent e) {
        removeSelectionListener(smListener);  //avoid listener cycle
        tool = e.selectionAsTool();  //null if no tool
        entity = e.selectionAsAttributeEntity();  //null if no attribute entity
        setSelection(e.getSGPath());
        addSelectionListener(smListener);
      }
    };
    
    private TreeSelectionModel tsm;

  /**
   * Add communication between the viewerApps navigator and the SelectionManager.
   * @param navigator the navigator
   */
  public void setNavigator(final Navigator navigator) {
    // clean up previous
    if (tsm != null) tsm.removeTreeSelectionListener(navigatorListener);
    if (smListener != null) removeSelectionListener(smListener);
    if (navigator != null) {	  
      tsm = navigator.getTreeSelectionModel();
      //add listener to Navigator
      tsm.addTreeSelectionListener(navigatorListener);
      //add listener to SelectionManager
      smListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent e) {
          //convert selection into TreePath
          TreePath path = getTreePath((SceneTreeModel) navigator.getSceneTree().getModel());
          tsm.removeTreeSelectionListener(navigatorListener);  //avoid listener cycle
          tsm.setSelectionPath(path);
          tsm.addTreeSelectionListener(navigatorListener);
        }
      };
      addSelectionListener(smListener);
//      smListener.selectionChanged(new SelectionEvent(this, getSelection(), null, null, SelectionEvent.NO_SELECTION));
    }
    else setSelection(null);  //select default selection (navigator == null)
  }
  
  public SceneGraphPath getDefaultSelection() {
    return defaultSelection;
  }
  
  
  public void setDefaultSelection(SceneGraphPath defaultSelection) {
    this.defaultSelection = defaultSelection;
  }
  
  
  public SceneGraphPath getSelection() {
    return selection;
  }
  
  
  public void setSelection(SceneGraphPath selection) {
    if (selection == null)  //nothing selected
      this.selection = defaultSelection;
    else this.selection = selection;
    
    nothingSelected = (selection == null);
    
    selectionChanged();
  }


  /**
   * converts the path of the current selection into the 
   * corresponding treepath in the scene tree model
   * (model of the viewerApp's navigator)
   */
  private TreePath getTreePath(SceneTreeModel model) {
    Object[] newPath = model.convertSceneGraphPath(selection);
    return new TreePath(newPath);
  }
  
  
  public void addSelectionListener(SelectionListener listener)  {
    if (listeners.contains(listener)) return;
    listeners.add(listener);
  }

  
  public void removeSelectionListener(SelectionListener listener) {
    listeners.remove(listener);
  }
  
  
  public void selectionChanged() {
    
    selectionObserver.setPath(this.selection);  //update observed path
    
    if (!listeners.isEmpty()) {
      for (int i = 0; i<listeners.size(); i++)  {
        SelectionListener l = listeners.get(i);
        l.selectionChanged(new SelectionEvent(this, this.selection, tool, entity, getCurrentType()));
      }
    }
    
    if (renderSelection) {
      if (nothingSelected) { 
        if (selectionKit != null) selectionKit.setVisible(false);
      }
      else {  //something selected
        updateBoundingBox();
        selectionKit.setVisible(true);
        if (viewer != null) viewer.render();  //render auxiliary root
      }
    }
  }

  
  /**
   * Returns the current selection type (static field of {@link SelectionEvent})
   */
  public int getCurrentType() {
    int type;
    if (tool == null && entity == null) 
      type = (nothingSelected) ? NO_SELECTION : DEFAULT_SELECTION; 
    else type = (tool != null) ? TOOL_SELECTION : ENTITY_SELECTION;
    
    return type;
  }
  

  public void setAuxiliaryRoot(SceneGraphComponent aux) {
    auxiliaryRoot = aux;
    if (auxiliaryRoot != null && selectionKit != null) 
      auxiliaryRoot.addChild(selectionKit);
  }


  public void setViewer(Viewer viewer) {
    this.viewer = viewer;
  }
  
  
  public void transformationMatrixChanged(TransformationEvent ev) {
    if (!renderSelection || selectionKit == null) 
      return; 
    
    if (selectionKit.getTransformation() != null)
      selectionKit.getTransformation().setMatrix(selection.getMatrix(null));
  }

  
  private void updateBoundingBox() {
    
    if (selectionKit == null) {
      //set up representation of selection in scene graph
      selectionKit = new SceneGraphComponent();
      selectionKit.setName("selection");
      Appearance app = new Appearance();
      app.setAttribute(CommonAttributes.EDGE_DRAW,true);
      app.setAttribute(CommonAttributes.FACE_DRAW,false);
      app.setAttribute(CommonAttributes.VERTEX_DRAW,false);
      app.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR, 1.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x6666);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
      app.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
      selectionKit.setAppearance(app);
      selectionKit.setTransformation(new Transformation());
      
      if (auxiliaryRoot != null) auxiliaryRoot.addChild(selectionKit);
    }
    
    Rectangle3D bbox = GeometryUtility.calculateChildrenBoundingBox( selection.getLastComponent() ); 
    
    IndexedFaceSet box = null;
    box = IndexedFaceSetUtility.representAsSceneGraph(box, bbox);
    
    selectionKit.setGeometry(box);
    transformationMatrixChanged(null);  //selectionKit.setTransformation()
    if (viewer != null) viewer.render();  //render auxiliary root
  }
  
  
  public boolean isRenderSelection() {
    return renderSelection;
  }

  
  public void setRenderSelection(boolean renderSelection) {
    this.renderSelection = renderSelection;
    if (renderSelection) {
      updateBoundingBox();
      transformationMatrixChanged(null);  //update transformation matrix
    }
    if (selectionKit != null) selectionKit.setVisible(renderSelection);
    if (viewer != null) viewer.render();  //render auxiliary root
  }

  
  public AttributeEntity getEntity() {
    return entity;
  }

  
  public Tool getTool() {
    return tool;
  }

  
  public boolean isNothingSelected() {
    return nothingSelected;
  }

}