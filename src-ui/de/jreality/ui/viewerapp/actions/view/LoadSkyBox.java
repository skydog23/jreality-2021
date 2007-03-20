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


package de.jreality.ui.viewerapp.actions.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.FileFilter;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.util.Input;


/**
 * Loads a skybox.
 * 
 * @author msommer
 */
public class LoadSkyBox extends AbstractJrAction {

	private SceneGraphComponent sceneRoot;
	 
  public LoadSkyBox(String name, SceneGraphComponent sceneRoot, Component frame) {
    
    super(name, frame);
    this.sceneRoot = sceneRoot;
    
    setShortDescription("Load skybox");
  }


  public void actionPerformed(ActionEvent e) {
    
  	File file = FileLoaderDialog.loadFile(parentComp, false, new FileFilter("ZIP archives", "zip"));
    if (file == null) return;  //dialog cancelled
  	
    //get image data
  	ImageData[] imgs;
		try {
			imgs = TextureUtility.createCubeMapData(Input.getInput(file));
		} catch (Exception ex) {
			System.err.println("Couldn't create cube map data from zip-file");
			ex.printStackTrace();
			return;
		}
  	
  	//get root appearance
		Appearance app = sceneRoot.getAppearance();
  	if (app == null) {
  		app = new Appearance("root appearance");
  		ShaderUtility.createRootAppearance(app);
  		sceneRoot.setAppearance(app);
  	}
  	
  	TextureUtility.createSkyBox(app, imgs);
  }

}