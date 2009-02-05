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


package de.jreality.geometry;

import java.awt.Color;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

/**
 * This factory classes can be used to create and edit instances of {@link IndexedFaceSet}.  See {@link IndexedLineSetFactory} and
 * {@link PointSetFactory} for functionality inherited from the superclasses.
 * <p>
 * Faces are specified using the method {@link #setFaceIndices(int[][])} and its variants. 
 * <p>
 * There are methods for setting the built-in face attributes normals, colors, and labels.
 * Texture coordinates can have fiber length 2, 3, or 4.  Normals in euclidean case must have fiber length 3; otherwise they should have 
 * length 4. Labels are represented by an array of type <code>String[]</code>, and are displayed at the center of the face.
 * <p>
 * There are a number of boolean methods to control whether the factory generates various types of derivative information:
 * <ul>
 * <li>{@link #setGenerateAABBTree(boolean)}</li>
 * <li>{@link #setGenerateFaceLabels(boolean)}</li>
 * <li>{@link #setGenerateFaceNormals(boolean)}</li>
 * <li>{@link #setGenerateVertexNormals(boolean)}</li>
 * </ul>
 * <p>
 * By default, all these values are <code>false</code>.
 * <p>
 * The AABBTree is stored as an {@link Attribute} within the geometry and is used to optimize picking.
 * <p>
 * The face labels show the index of the face within the face array and are displayed at the midpoint of the face.
 * <p> 
 * The face normals are generated using the cross product of the specified metric (see {@link #setMetric(int)}.
 * <p>
 * The vertex normals are generated based on the face normals, by averaging using the combinatorial information in the face index array.
 * @author gunn
 *
 */
public class IndexedFaceSetFactory extends AbstractIndexedFaceSetFactory {

	public IndexedFaceSetFactory() {
		super();
	}

	/* vertex attributes */
	
	public void setVertexCount( int count ) {
		super.setVertexCount(count);
	}
	
	public void setVertexAttribute( Attribute attr, DataList data ) {
		super.setVertexAttribute( attr, data );
	}
	
	public void setVertexAttribute(Attribute attr, double [] data ) {
		super.setVertexAttribute( attr, data );
	}
	
	public void setVertexAttribute(Attribute attr,  double [][] data ) {
		super.setVertexAttribute( attr, data);
	}
	
	public void setVertexAttributes(DataListSet dls ) {
		super.setVertexAttributes( dls );
	}

	public void setVertexCoordinates( DataList data ) {
		super.setVertexCoordinates(data);
	}
	
	public void setVertexCoordinates( double [] data ) {
		super.setVertexCoordinates( data );
	}
	
	public void setVertexCoordinates( double [][] data ) {
		super.setVertexCoordinates( data );
	}
	
	public void setVertexNormals( DataList data ) {
		super.setVertexNormals(data);
	}
	
	public void setVertexNormals( double [] data ) {
		super.setVertexNormals( data );
	}
	
	public void setVertexNormals( double [][] data ) {
		super.setVertexNormals( data );
	}
	
	public void setVertexColors( DataList data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexColors( double [] data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexColors( Color [] data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexColors( double [][] data ) {
		super.setVertexColors( data );
	}
	
	public void setVertexTextureCoordinates( DataList data ) {
		super.setVertexTextureCoordinates( data );
	}
	
	public void setVertexTextureCoordinates( double [] data ) {
		super.setVertexTextureCoordinates( data );
	}
	
	public void setVertexTextureCoordinates( double [][] data ) {
		super.setVertexTextureCoordinates( data );
	}

	public void setVertexLabels( String [] data ) {
		super.setVertexLabels( data );
	}

	public void setVertexRelativeRadii( double [] data ) {
		super.setVertexRelativeRadii( data );
	}
	/* edge attributes */

	public void setEdgeCount( int count ) {
		super.setEdgeCount(count);
	}
	
	@Override
	public void setEdgeAttribute(Attribute attr, DataList data)	{
		super.setEdgeAttribute(attr, data);
	}
	
	@Override
	public void setEdgeAttribute(Attribute attr, double[] data) {
		super.setEdgeAttribute(attr, data);
	}

	@Override
	public void setEdgeAttribute(Attribute attr, double[][] data) {
		super.setEdgeAttribute(attr, data);
	}

	public void setEdgeIndices( int[][] data ) {
		super.setEdgeIndices(data);
	}
	
	public void setEdgeIndices( int[] data, int pointCountPerLine ) {
		super.setEdgeIndices(data, pointCountPerLine );
	}
	
	public void setEdgeIndices( int[] data ) {
		super.setEdgeIndices( data );
	}
	
	
	public void setEdgeColors( DataList data ) {
		super.setEdgeColors( data );
	}
	
	public void setEdgeColors( double [] data ) {
		super.setEdgeColors(data);
	}
	
	public void setEdgeColors( Color [] data ) {
		super.setEdgeColors( data );
	}
	
	public void setEdgeColors( double [][] data ) {
		super.setEdgeColors(data);
	}

	public void setEdgeLabels( String[] data ) {
		super.setEdgeLabels( data );
	}
	
	public void setEdgeRelativeRadii( double [] data ) {
		super.setEdgeRelativeRadii( data );
	}

	/* face attributes */
	
	public void setFaceCount( int count ) {
		super.setFaceCount( count );
	}
	
	/**
	 * Superclass methods are protected so we override to make public
	 * Documentation is lacking ...
	 */
	@Override
	public void setFaceAttribute( Attribute attr, DataList data) {
		super.setFaceAttribute( attr, data );
	}
	
	@Override
	public void setFaceAttribute(Attribute attr, double[] data) {
		super.setFaceAttribute(attr, data);
	}

	@Override
	public void setFaceAttribute(Attribute attr, double[][] data) {
		super.setFaceAttribute(attr, data);
	}

	public void setFaceAttributes(DataListSet dls ) {
		super.setFaceAttributes(dls);
	}
	
	public void setFaceIndices( DataList data ) {
		super.setFaceIndices(data);
	}
	
	public void setFaceIndices( int[][] data ) {
		super.setFaceIndices(data);
	}
	
	public void setFaceIndices( int[] data, int pointCountPerFace ) {
		super.setFaceIndices(data, pointCountPerFace );
	}
	
	public void setFaceIndices( int[] data ) {
		super.setFaceIndices(data);
	}
	
	public void setFaceNormals( DataList data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceNormals( double [] data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceNormals( double [][] data ) {
		super.setFaceNormals(data);
	}
	
	public void setFaceColors( DataList data ) {
		super.setFaceColors(data);
	}
	
	public void setFaceColors( double [] data ) {
		super.setFaceColors(data);
	}
	
	public void setFaceColors( Color [] data ) {
		super.setFaceColors( data );
	}
	
	public void setFaceColors( double [][] data ) {
		super.setFaceColors(data);
	}

	public void setFaceLabels( String [] data ) {
		super.setFaceLabels( data );
	}

}
