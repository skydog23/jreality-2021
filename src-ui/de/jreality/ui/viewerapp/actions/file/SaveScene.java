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
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.jreality.io.JrScene;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.WriterJRS;


/**
 * Saves the current scene.
 * 
 * @author msommer
 */
public class SaveScene extends AbstractJrAction {

  private ToolSystemViewer viewer;
  

  public SaveScene(String name, ToolSystemViewer viewer, Component parentComp) {
    super(name, parentComp);
    
    if (viewer == null) 
      throw new IllegalArgumentException("Viewer is null!");
    this.viewer = viewer;
    
    setShortDescription("Save scene as a file");
    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
  }

//  public SaveScene(String name, ViewerApp v) {
//    this(name, v.getViewer(), v.getFrame());
//  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {
    File file = FileLoaderDialog.selectTargetFile(parentComp, "jrs", "jReality scene files");
    if (file == null) return;  //dialog cancelled
   
    try {
      FileWriter fw = new FileWriter(file);
      WriterJRS writer = new WriterJRS();
      JrScene s = new JrScene(viewer.getSceneRoot());
      s.addPath("cameraPath", viewer.getCameraPath());
      s.addPath("avatarPath", viewer.getAvatarPath());
      s.addPath("emptyPickPath", viewer.getEmptyPickPath());
      writer.writeScene(s, fw);
      fw.close();
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(parentComp, "Save failed: "+ioe.getMessage());
    }
  }

}