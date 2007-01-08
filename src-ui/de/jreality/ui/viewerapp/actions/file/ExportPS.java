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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import de.jreality.scene.Viewer;
import de.jreality.softviewer.PSRenderer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;


/**
 * Exports the scene displayed in a viewer as a post script file.
 * 
 * @author hoffmann
 */
public class ExportPS extends AbstractJrAction {
  
  private Viewer viewer;
  
  
  public ExportPS(String name, Viewer viewer, Component parentComp) {
    super(name, parentComp);
    
    if (viewer == null)
      throw new IllegalArgumentException("Viewer is null!");
    this.viewer = viewer;
    
    setShortDescription("Export the current scene as PostScript file");
  }
  
//  public ExportPS(String name, ViewerApp v) {
//    this(name, v.getViewerSwitch(), v.getFrame());
//  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {
    File file = FileLoaderDialog.selectTargetFile(parentComp, "ps", "PostScript Files");
    if (file == null)
      return;
    // try {
    Dimension d = viewer.getViewingComponentSize();
    PSRenderer rv;
    try {
      rv = new PSRenderer(new PrintWriter(file), d.width, d.height);
      rv.setCameraPath(viewer.getCameraPath());
      rv.setSceneRoot(viewer.getSceneRoot());
      rv.setAuxiliaryRoot(viewer.getAuxiliaryRoot());
      // rv.initializeFrom(viewer);
      rv.render();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
    // } catch (IOException ioe) {
    // JOptionPane.showMessageDialog(frame, "Save failed:
    // "+ioe.getMessage());
    // }
  }
  
}