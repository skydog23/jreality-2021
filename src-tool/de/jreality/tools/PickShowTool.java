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


package de.jreality.tools;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;

public class PickShowTool extends AbstractTool {

  SceneGraphComponent c = new SceneGraphComponent();
  Appearance a = new Appearance();
  
  private boolean attached;
  
  public PickShowTool(String activationAxis, double radius) {
    super(activationAxis == null ? null : InputSlot.getDevice(activationAxis));
    addCurrentSlot(InputSlot.getDevice("PointerTransformation"));
    c.addChild(Primitives.sphere(radius, 0, 0, 0));
    c.setAppearance(a);
    a.setAttribute("pickable", false);
    a.setAttribute(CommonAttributes.FACE_DRAW, true);
  }
  public PickShowTool(String activationAxis) {
    this(activationAxis, 0.05);
  }
  
  public void activate(ToolContext tc) {
    perform(tc);
  }
  
  public void perform(ToolContext tc) {
    PickResult pr = tc.getCurrentPick();
    if (pr == null) {
      assureDetached(tc);
      return;
    }
    assureAttached(tc);
    switch (pr.getPickType()) {
    case PickResult.PICK_TYPE_FACE:
      c.getAppearance().setAttribute("diffuseColor", Color.yellow);
      break;
    case PickResult.PICK_TYPE_LINE:
      c.getAppearance().setAttribute("diffuseColor", Color.green);
      break;
    case PickResult.PICK_TYPE_POINT:
      c.getAppearance().setAttribute("diffuseColor", Color.magenta);
      break;
    case PickResult.PICK_TYPE_OBJECT:
      c.getAppearance().setAttribute("diffuseColor", Color.red);
      break;
    default:
      c.getAppearance().setAttribute("diffuseColor", Color.black);
    }
    double[] worldCoordinates = pr.getWorldCoordinates();
    MatrixBuilder.euclidean().translate(worldCoordinates).assignTo(c);
  }

  public void deactivate(ToolContext tc) {
    assureDetached(tc);
  }
  
  private void assureAttached(ToolContext tc) {
    if (!attached) tc.getViewer().getSceneRoot().addChild(c);
    attached = true;
  }
  public void assureDetached(ToolContext tc) {
    if (attached) {
      tc.getViewer().getSceneRoot().removeChild(c);
      tc.getViewer().render();
    }
    attached = false;
  }

}
