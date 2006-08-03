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


package de.jreality.scene.tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public abstract class AbstractTool implements Tool {

  private InputSlot activationSlot;
  private transient List currentSlots=Collections.EMPTY_LIST;
  private final transient HashMap descriptions=new HashMap();
  private String description = "No description!";
  
  public AbstractTool(InputSlot activationSlot) {
    this.activationSlot=activationSlot;
  }
  
  public InputSlot getActivationSlot() {
    return activationSlot;
  }
  public List getCurrentSlots() {
    return currentSlots;
  }
  
  protected void addCurrentSlot(InputSlot slot) {
    addCurrentSlot(slot, null);
  }
  
  protected void addCurrentSlot(InputSlot slot, String description) {
    if (currentSlots.isEmpty()) currentSlots = new LinkedList();
    if (!currentSlots.contains(slot)) currentSlots.add(slot);
    setDescription(slot, description);
  }

  protected void setDescription(InputSlot slot, String description) {
    descriptions.put(slot, description != null ? description : "<no description>");
  }
  
  protected void removeCurrentSlot(InputSlot slot) {
    currentSlots.remove(slot);
  }
  public void activate(ToolContext tc) {
  }
  public void perform(ToolContext tc) {
  }
  public void deactivate(ToolContext tc) {
  }
  public String getFullDescription() {
    StringBuffer sb = new StringBuffer();
    sb.append(getClass().getName()).append(": ").append(getDescription()).append('\n');
    sb.append(": always active=").append(activationSlot==null).append('\n');
    if (activationSlot != null) {
      sb.append("activation="+activationSlot.getName());
      sb.append(" [").append(getDescription(activationSlot)).append("]\n");
    }
    sb.append("current slots:").append('\n');
    for (Iterator it = getCurrentSlots().iterator(); it.hasNext(); ) {
      InputSlot is = (InputSlot) it.next();
      sb.append("  slot="+is.getName()).append(" [").append(getDescription(is)).append("]\n");
    }
    return sb.toString();
  }
  public String getDescription(InputSlot slot) {
    return (String) descriptions.get(slot);
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
	this.description=description;
  }
}