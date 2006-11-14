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

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.reader.Readers;
import de.jreality.util.Secure;


public class FileLoaderDialog {
	
  static File lastDir = new File(Secure.getProperty("jreality.data", "/net/MathVis/data/testData3D"));
  
  
  public static JFileChooser createFileChooser() {
    FileFilter ff = new FileFilter(){
      public boolean accept(File f) {
        if (f.isDirectory()) return true;
        String filename = f.getName().toLowerCase();
        return (Readers.findFormat(filename) != null);
      }
      public String getDescription() {
        return "jReality 3D data files";
      }
    };
    return createFileChooser(ff);
  }
  
  
  static JFileChooser createFileChooser(final String ext, final String description) {
      FileFilter ff = new FileFilter(){
        public boolean accept(File f) {
          return (f.isDirectory() || 
              f.getName().endsWith("."+ext) || 
              f.getName().endsWith("."+ext.toLowerCase()) ||
              f.getName().endsWith("."+ext.toUpperCase()));
        }
        public String getDescription() {
          return description;
        }
      };
      return createFileChooser(ff);
  }

  
  public static JFileChooser createFileChooser(FileFilter... ff) {
    FileSystemView view = FileSystemView.getFileSystemView();
    JFileChooser chooser = new JFileChooser(!lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
    for (int i = 0; i < ff.length; i++)
      chooser.addChoosableFileFilter(ff[i]);
    chooser.setFileFilter(chooser.getAcceptAllFileFilter());
    
    return chooser;
  }
  
  
  public static File[] loadFiles(Component parent) {
    JFileChooser chooser = createFileChooser();
    chooser.setMultiSelectionEnabled(true);
    chooser.showOpenDialog(parent);
    File[] files = chooser.getSelectedFiles();
    lastDir = chooser.getCurrentDirectory();
    return files;
  }
  
  
  private static File selectTargetFile(Component parent,JFileChooser chooser) {
    chooser.setMultiSelectionEnabled(false);
    chooser.showSaveDialog(parent);
    lastDir = chooser.getCurrentDirectory();
    return chooser.getSelectedFile();
  }
  
  
  public static File selectTargetFile(Component parent) {
      JFileChooser chooser = createFileChooser();
      return selectTargetFile(parent, chooser);
  }
  
  
  public static File selectTargetFile(Component parent, String extension, String description) {
      JFileChooser chooser = createFileChooser(extension, description);
      return selectTargetFile(parent, chooser);
  }
  
  
  public static File selectTargetFile(Component parent, FileFilter... ff) {
    JFileChooser chooser = createFileChooser(ff);
    return selectTargetFile(parent, chooser);
  }
  
}