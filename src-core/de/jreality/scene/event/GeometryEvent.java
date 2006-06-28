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


package de.jreality.scene.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.jreality.scene.Geometry;

/**
 * The event object containing information about the changed
 * geometry data. The event contains the attribute keys of changed
 * geometry features. The key can be used to query the actual values
 * from the source geometry.
 * @author pietsch
 */
public class GeometryEvent extends SceneEvent {
    private final Geometry geometry;
    private Set vertexKeys;
    private Set vertexView;
    private Set edgeKeys;
    private Set edgeView;
    private Set faceKeys;
    private Set faceView;
    private Set geomKeys;
    private Set geomView;
    /**
     * @param source
     */
    public GeometryEvent(Geometry source, Set chgVertexAttrKeys,
      Set chgEdgeAttrKeys, Set chgFaceAttrKeys, Set chgGeomAttrKeys) {
        super(source);
        geometry=source;
        vertexKeys=chgVertexAttrKeys!=null?
          new HashSet(chgVertexAttrKeys): Collections.EMPTY_SET;
        edgeKeys  =chgEdgeAttrKeys!=null?
          new HashSet(chgEdgeAttrKeys):   Collections.EMPTY_SET;
        faceKeys  =chgFaceAttrKeys!=null?
          new HashSet(chgFaceAttrKeys):   Collections.EMPTY_SET;
        geomKeys  =chgGeomAttrKeys!=null?
          new HashSet(chgGeomAttrKeys):   Collections.EMPTY_SET;
    }

    public Set getChangedVertexAttributes() {
      if(vertexView==null)
        vertexView=Collections.unmodifiableSet(vertexKeys);
      return vertexView;
    }

    public Set getChangedEdgeAttributes() {
      if(edgeView==null)
        edgeView=Collections.unmodifiableSet(edgeKeys);
      return edgeView;
    }
  
    public Set getChangedFaceAttributes() {
      if(faceView==null)
        faceView=Collections.unmodifiableSet(faceKeys);
      return faceView;
    }
  
    public Set getChangedGeometryAttributes() {
      if(geomView==null)
        geomView=Collections.unmodifiableSet(geomKeys);
      return geomView;
    }

    public Geometry getGeometry() {
      return geometry;
    }

}
