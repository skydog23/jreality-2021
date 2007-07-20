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


package de.jreality.ui.viewerapp.actions.file;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.RemoveDuplicateInfo;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.util.CameraUtility;
import de.jreality.util.PickUtility;


/**
 * Loads one or several files into the scene and optionally merges indexed face & line sets
 * (adds the files as children to the selection managers default selection, 
 * which is usually the scene node).
 * 
 * @author msommer
 */
public class LoadFile extends AbstractJrAction {


  private SceneGraphComponent parentNode;
  private Viewer viewer;
  
  private JComponent options; 
  private JCheckBox mergeFaceSets;
  private JCheckBox mergeFaceSetsWithNormals;
  private JCheckBox removeDublicateVertices;
  private JCheckBox removeDublicateVerticesWithNormals;
  private JCheckBox callEncompass;
  

  public LoadFile(String name, SceneGraphComponent parentNode, Viewer viewer, Component parentComp) {
    super(name, parentComp);
    this.parentNode = parentNode;
    this.viewer = viewer;
    
    setShortDescription("Load one or more files");
    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
  }

  public LoadFile(String name, SceneGraphComponent parentNode, Viewer viewer) {
	  this(name, parentNode, viewer, null);
  }
  
  public LoadFile(String name, SceneGraphComponent parentNode, Component parentComp) {
	  this(name, parentNode, null, parentComp);
  }
  
  public LoadFile(String name, SceneGraphComponent parentNode) {
	  this(name, parentNode, null, null);
  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {

    if (options == null) options = createAccessory();
    mergeFaceSets.setSelected(false);
    mergeFaceSetsWithNormals.setSelected(false);
    mergeFaceSetsWithNormals.setEnabled(false);
    removeDublicateVertices.setSelected(false);
    removeDublicateVertices.setEnabled(false);
    removeDublicateVerticesWithNormals.setSelected(false);
    removeDublicateVerticesWithNormals.setEnabled(false);
    
    File[] files = FileLoaderDialog.loadFiles(parentComp, options);
    if (files == null) return;  //dialog cancelled
    
    for (int i = 0; i < files.length; i++) {
      try {
        SceneGraphComponent sgc = Readers.read(files[i]);
        if (sgc==null) throw new IOException("Could not read "+files[i].getPath());  //return;
        
        GeometryMergeFactory mFac = new GeometryMergeFactory();
        SceneGraphComponent comp = new SceneGraphComponent();
        if (mergeFaceSets.isSelected()){
        	if(!mergeFaceSetsWithNormals.isSelected())
        		mFac.setGenerateVertexNormals(false);
        	IndexedFaceSet geo = mFac.mergeIndexedFaceSets(sgc);
        	if(removeDublicateVertices.isSelected()){
        		if(removeDublicateVerticesWithNormals.isSelected())
        			geo = RemoveDuplicateInfo.removeDuplicateVertices(geo,Attribute.NORMALS);
        		else geo = RemoveDuplicateInfo.removeDuplicateVertices(geo);
        	}
        	comp.setGeometry(geo);	
        	sgc=comp;
        } 
        System.out.println("READ finished.");
        parentNode.addChild(sgc);
        
        PickUtility.assignFaceAABBTrees(sgc);
        
        if (callEncompass.isSelected() && viewer != null) {
        	ToolSystem ts = ToolSystem.getToolSystemForViewer(viewer);
        	if (ts != null) {
        	CameraUtility.encompass(ts.getAvatarPath(),
        			ts.getEmptyPickPath(),
        			viewer.getCameraPath(),
        			1.75, viewer.getSignature());
        	}
        }
      } 
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(parentComp, "Failed to load file: "+ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  
  private JComponent createAccessory() {
    Box box = Box.createVerticalBox();
    TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options");
    box.setBorder(title);

    mergeFaceSets = new JCheckBox("merge geometries");
    mergeFaceSetsWithNormals = new JCheckBox("garantee vertex normals");
    removeDublicateVertices = new JCheckBox("remove dublicate vertices");
    removeDublicateVerticesWithNormals = new JCheckBox("respect vertex normals");
    
    mergeFaceSets.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ev) {
				boolean editable = mergeFaceSets.isSelected();
				mergeFaceSetsWithNormals.setEnabled(editable);
				removeDublicateVertices.setEnabled(editable);
				removeDublicateVerticesWithNormals.setEnabled(editable && removeDublicateVertices.isSelected());
			}
		});
    removeDublicateVertices.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				removeDublicateVerticesWithNormals.setEnabled(removeDublicateVertices.isSelected());
			}
		});
    
    callEncompass = new JCheckBox("encompass scene");
    callEncompass.setSelected(true);

    box.add(mergeFaceSets);
    Box tmp = Box.createHorizontalBox();
    tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
    tmp.add(new JLabel("  "));
    tmp.add(mergeFaceSetsWithNormals);
    box.add(tmp);
    tmp = Box.createHorizontalBox();
    tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
    tmp.add(new JLabel("  "));
    tmp.add(removeDublicateVertices);    
    box.add(tmp);
    tmp = Box.createHorizontalBox();
    tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
    tmp.add(new JLabel("    "));
    tmp.add(removeDublicateVerticesWithNormals);
    box.add(tmp);
    box.add(Box.createVerticalStrut(10));
    box.add(callEncompass);
    box.validate();
    
    return box;
  }

}