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


package de.jreality.reader.vrml;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.FactoredMatrix;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.CommonAttributes;

public class VRMLHelper {
	public static boolean verbose = true;
	
	public static final int DEFAULT = 0;
	public static final int OVERALL = 1;
	public static final int PER_PART = 2;
	public static final int PER_PART_INDEXED = 3;
	public static final int PER_FACE = 4;
	public static final int PER_FACE_INDEXED = 5;
	public static final int PER_VERTEX = 6;
	public static final int PER_VERTEX_INDEXED = 7;

	public VRMLHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	// convert a List of Integer objects into an int[]
	public static int[] listToIntArray(List l)		{
		int[] foo = new int[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Integer)iter.next()).intValue();
		}
		return foo;
	}
	public static double[] listToDoubleArray(List l)		{
		double[] foo = new double[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Double)iter.next()).doubleValue();
		}
		return foo;
	}
	
	public static double[][] listToDoubleArrayArray(List l)		{
		double[][] foo = new double[l.size()][];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((double[])iter.next());
		}
		return foo;
	}

	public static Color[] listToColorArray(List l)		{
		Color[] foo = new Color[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Color)iter.next());
		}
		return foo;
	}
	

	public static int[][] convertIndices(int[] input)	{
		// count the number of negative entries
		int count = 0;
		List breakpoints = new Vector();
		for (int i=0; i<input.length; ++i)	{
			if (input[i] < 0) breakpoints.add(new Integer(i));
		}
		count = breakpoints.size();
		int[][] output = new int[count][];
		int oldIndex, newIndex;
		newIndex = -1;
		int faceCount = 0;
		Iterator iter = breakpoints.iterator();
		while (iter.hasNext())	{
			oldIndex = newIndex+1;
			newIndex = ((Integer) iter.next()).intValue();
			output[faceCount] = new int[newIndex - oldIndex];
			for (int j = oldIndex; j<newIndex; ++j)	{
				output[faceCount][j-oldIndex] = input[j];
				if (verbose) System.err.print(input[j]+" ");
			}
			faceCount++;
			if (verbose) System.err.println("");
		}
		return output;
	}

	public static int[] reallocate(int[] array)	{
		int n = array.length;
		int[] newarray = new int[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

	public static double[] reallocate(double[] array)	{ /// Bernd
		int n = array.length;
		double[] newarray = new double[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

	public static Color[] reallocate(Color[] array)	{ ///Bernd
		int n = array.length;
		Color[] newarray = new Color[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

	public static String[] reallocate(String[] array)	{ ///Bernd
		int n = array.length;
		String[] newarray = new String[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

	public static double[][] reallocate(double[][] array)	{ ///Bernd
		int n = array.length;
		double[][] newarray = new double[n*2][];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

//	public static void viewSceneGraph(SceneGraphComponent r) {
//		InteractiveViewerDemo ivd = new InteractiveViewerDemo();
//		if (r.getTransformation() == null)
//			r.setTransformation(new Transformation());
//		if (r.getAppearance() == null)	
//			r.setAppearance(new Appearance());
//		r.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
//		ivd.viewer.getSceneRoot().addChild(r);
//		ivd.viewer.getCameraPath().getLastComponent().addChild(ivd.makeLights());
//		SceneGraphPath toSGC = new SceneGraphPath();
//		toSGC.push(ivd.viewer.getSceneRoot());
//		toSGC.push(r);
//		ivd.viewer.getSelectionManager().setSelection(toSGC);
//		ivd.viewer.getSelectionManager().setDefaultSelection(toSGC);
//		CameraUtility.encompass(ivd.viewer);
//		ivd.viewer.render();
//		
//	}

	/**
	 * braucht man fuer sfbitmaskValue.
	 * setzt in mask den i-ten Eintrag auf true falls 
	 * key dem i-ten Eintrag in code entspricht
	 * @param code  Liste gueltiger Namen(geordnet!)
	 * @param mask  enthaelt die gesammelten Aussagen
	 *  ueber gefundene Namen
	 * @param key   zu vergleichender Name
	 */
	public static void checkFlag(String [] code,boolean[] mask,String key){
		boolean hit=false;
		for (int i= 0;i<code.length;i++){
			if (key.equals(code[i])){
				mask[i]=true;
				hit=true;
				break;
			}
		}
		if (!hit)System.out.println("unknown AttributValue:"+key);
	}
	/**
	 * braucht man fuer sfenumValue
	 * liefert die Nummer der Stelle von key in code 
	 * @param code Liste gueltiger Namen (geordnet!)
	 * @param key  zu findender Name
	 * @return Nummer der Position (erste Position ist default)
	 */
	public static int getEnum(String[] code, String key){
		int n=0;
		for (int i=0;i<code.length;i++){
			if (key.equals(code[i]))
				n=i;
		}
		return n;
	}
	/**
	 * IndizeAbschnitte werden in VRML1.0 durch '-1' getrennt.
	 * Hier werden sie zu einer Doppelliste.
	 * 
	 * @param list eindimensionale Liste (trennung durch '-1') 
	 * @return nicht quadratische zweidimensionale Liste
	 * 
	 */
	public static int[][] convertIndexList (int[] list){
		// -1 trennt die Teile
		int n=0;// # parts
		for (int i=0;i<list.length;i++)// zaehlt die '-1'-en
			if (list[i]==-1) n++;
		if (list.length==0)
			return new int[][]{};
		if (list[list.length-1]!=-1) n++;// -1 am ende ist optional
		int[][] indices = new int[n][];
		Vector v;
		int k=0;// current index 
		for(int i=0;i<n;i++){// fuer alle Listen
			v= new Vector();
			while ((k<list.length)&&(list[k]!=-1)){
				v.add(new Integer(list[k]));
				k++;
			}
			indices[i]= new int[v.size()];
			for (int j=0;j<v.size();j++)
				indices[i][j]=((Integer)v.get(j)).intValue();
			k++;// -1 ueberspringen
		}
		return indices;
	}
	
	/**
	 * Erweiterung des Zylinders:
	 * Optionaler Boden,  Deckel, Mantel. 
	 * @param side
	 * @param top
	 * @param bottom
	 * @param n	 #Unterteilung (feinheit)
	 * @return Zylinder
	 */
	public static IndexedFaceSet cylinder(boolean side,boolean top,boolean bottom,int n) {
		int rn = n+1;
		double[] verts = new double[2*3*rn];
		double angle = 0, delta = Math.PI*2/(n);
		for (int i = 0 ;i<rn; ++i)	{
			angle = i*delta;
			verts[3*(i+rn)]   = verts[3*i]   = Math.cos(angle);
			verts[3*(i+rn)+2] = verts[3*i+2] = Math.sin(angle);
			verts[3*i+1] = 0.5;
			verts[3*(i+rn)+1] = -0.5;
		}
		QuadMeshFactory qmf = new QuadMeshFactory();//Pn.EUCLIDEAN, n+1, 2, true, false);
		qmf.setULineCount(n+1);
		qmf.setVLineCount(2);
		qmf.setClosedInUDirection(true);
		qmf.setVertexCoordinates(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.update();
		IndexedFaceSet geo1= qmf.getIndexedFaceSet();
		double[][] verts2 = new double[n][3];
		double[][] verts3 = new double[n][3];
		for (int  i =0; i<n; ++i)	{
			angle = 2 * (i+.5) * Math.PI/n;
			verts2[i][0] = verts3[i][0] = Math.cos(angle);
			verts2[i][2] = verts3[i][2] = Math.sin(angle);
			verts2[i][1] = 0.5;
			verts3[i][1] = -0.5;
		}
		IndexedFaceSet geo2= IndexedFaceSetUtility.constructPolygon(verts2);
		IndexedFaceSetUtility.assignSmoothVertexNormals(geo2,0,0);
		IndexedFaceSet geo3= IndexedFaceSetUtility.constructPolygon(verts3);
		IndexedFaceSetUtility.assignSmoothVertexNormals(geo3,0,0);
		// GeometrieTeile auswaehlen
		Vector v=new Vector();
		if (side)  v.add(geo1);
		if (top)   v.add(geo2);
		if (bottom)v.add(geo3);
		IndexedFaceSet[] geos= new IndexedFaceSet[v.size()];
		for (int i=0;i<v.size();i++){
			geos[i]=(IndexedFaceSet)v.get(i);
			}
		if(geos.length==0)
			return null; 
		IndexedFaceSet result=IndexedFaceSetUtility.mergeIndexedFaceSets(geos);
		return result;
		}
	/**
	 * Cone mit optionalem Mantel bzw Boden
	 * @param sidesdraw
	 * @param bottomdraw
	 * @param n		feinheit der Unterteilung
	 * @return Kegel
	 */
	public static IndexedFaceSet cone(boolean sidesdraw, boolean bottomdraw,int n) {
		// Points
		double[] tip= new double[]{0,1,0}; 
		double[][] vertsBottom = new double[n][];
		double angle = 0;
		for (int i=0;i<n;i++){
			angle = 2 * (i+.5) * Math.PI/n;
			vertsBottom[i] = new double[]{
					Math.cos(angle),0,Math.sin(angle) };
		}
		IndexedFaceSet sides=null;
		IndexedFaceSet bottom=null;
		IndexedFaceSetFactory ifsf=null;
		if (bottomdraw){
			int[][] indicesB = new int[1][];
			indicesB[0] = new int[n];
			for (int i = 0; i<n; ++i)	indicesB[0][i] = i;
			ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(n);
			ifsf.setFaceCount(1);
			ifsf.setVertexCoordinates(vertsBottom);
			ifsf.setFaceIndices(indicesB);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.setGenerateVertexNormals(true);
			ifsf.update();
			bottom= ifsf.getIndexedFaceSet();
		}
		if (sidesdraw){
			double[][] vertsSides = new double[n+1][];
			System.arraycopy(vertsBottom,0,vertsSides,0,n);
			vertsSides[n]=tip;
			int[][] indicesS = new int[n][];
			for (int i = 0; i<n; ++i)	{
				indicesS[i] = new int[3];
				indicesS[i][0] = i;
				indicesS[i][1] = (i+1)%n;
				indicesS[i][2] = n;
			}
			ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(n+1);
			ifsf.setFaceCount(n);
			ifsf.setVertexCoordinates(vertsSides);
			ifsf.setFaceIndices(indicesS);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateVertexNormals(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			sides= ifsf.getIndexedFaceSet();
		}
		if (sidesdraw && bottomdraw)
			return IndexedFaceSetUtility.mergeIndexedFaceSets(
					new	IndexedFaceSet[]{bottom,sides});
		if (bottomdraw)
			return bottom;
		if (sidesdraw)
			return sides;
		return null;
	}
	/**
	 * VRML kann mehrzeilige Labels.
	 * jReality noch nicht.
	 * Also werden Zeilen hintereinandergehaengt.
	 * @param ss
	 * @return
	 */
	public static String mergeStrings(String [] ss){
		String s=ss[0];
		for (int i=1;i<ss.length;i++){
			s=s+"   "+ss[i];
		}
		if(s.equals("")) return " ";
		return s;
	}  
	/**
	 * setzt die aktuell gueltigen Normalen fuer 
	 * IndexedFaceSetFactory
	 * zu gegebenen indizes ein.
	 * State enthealt: 
	 * 		die Normalen
	 * 		die binding Informationen
	 * 
	 * @param ifsf Factory die die normalen bekommen soll
	 * @param cIndex indizes der Farben
	 * @param nIndex indizes der Normalen
	 * @param state enthaelt die Daten
	 */
	public static void setNormals(IndexedFaceSetFactory ifsf,
			int [][] cIndex,int[][] nIndex,State state){
	int faceCount=cIndex.length;
	int VertexCount= state.coords.length;
	double[][] fNormals=new double[faceCount][3];
	double[][] vNormals=new double[VertexCount][3];
	switch (state.normalBinding) {
	case 1:// overall
	{	for (int i=0;i<faceCount;i++){
			fNormals[i][0]=state.normals[0][0];
			fNormals[i][1]=state.normals[0][1];
			fNormals[i][2]=state.normals[0][2];
		}
		ifsf.setFaceNormals(fNormals);
		//ifsf.setGenerateVertexNormals(true);
	}
	break;
	case 2:// per part
	case 4:// per face
	{	System.arraycopy(state.normals,0,fNormals,0,faceCount);
		ifsf.setFaceNormals(fNormals);
		//ifsf.setGenerateVertexNormals(true);
	}
	break;
	case 3:// per part indexed
	case 5:// per face indexed
	{	for (int i=0;i<faceCount;i++){
			fNormals[i][0]=state.normals[(nIndex[0][i])][0];
			fNormals[i][1]=state.normals[(nIndex[0][i])][1];
			fNormals[i][2]=state.normals[(nIndex[0][i])][2];
		}
		ifsf.setFaceNormals(fNormals);
		//ifsf.setGenerateVertexNormals(true);
	}
	break;
	case 6:// per Vertex
	{
		int m=0;
		for (int i=0;i<faceCount;i++){
			int faceLength=cIndex[i].length;
			for (int j=0;j<faceLength;j++){
				double [] n=state.normals[m];
				vNormals[cIndex[i][j]][0]=n[0];
				vNormals[cIndex[i][j]][1]=n[1];
				vNormals[cIndex[i][j]][2]=n[2];
				m++;
			}
		}
		ifsf.setVertexNormals(vNormals);
		ifsf.setGenerateFaceNormals(true);
	}
		break;
	case 0:// default
	case 7:// per Vertex indexed 
	{
		if (nIndex == null || nIndex.length != faceCount){
			//ifsf.setGenerateVertexNormals(true);
			ifsf.setGenerateFaceNormals(true); 
			break;
		}
		for (int i=0;i<faceCount;i++){
//			int k=faceCount-i-1;
			int faceLength=cIndex[i].length;
			for (int j=0;j<faceLength;j++){
//				int l=faceLength-1-j;
				double [] n=state.normals[nIndex[i][j]];
				vNormals[cIndex[i][j]][0]=n[0];
				vNormals[cIndex[i][j]][1]=n[1];
				vNormals[cIndex[i][j]][2]=n[2];
			}
		}
		ifsf.setVertexNormals(vNormals);
		ifsf.setGenerateFaceNormals(true);
	}
		break;
		default:
		break;
	}
}
	/**
	 * Setzt FarbListe in die gegebene Factory
	 * @param ifsf
	 * @param coordIndex Koordinaten-Indizierung
	 * 	zur KoordinatenListe in state
	 * @param colorIndex Farb-Indizierung
	 * 	zur FarbListe in state 
	 * @param state
	 */
	public static void setColors(IndexedFaceSetFactory ifsf,
			int [][] coordIndex,int[][] colorIndex,State state){
	int faceCount=coordIndex.length;
	int VertexCount= state.coords.length;
	Color[] fColors=new Color[faceCount];
	Color[] vColors=new Color[VertexCount];
	switch (state.materialBinding) {
	case 0:// default
	case 1:// overall
	break;
	case 2:// per part
	case 4:// per face
	{   if (state.diffuse.length>=faceCount){
			System.arraycopy(state.diffuse,0,fColors,0,faceCount);
			ifsf.setFaceColors(fColors);
	}	else System.err.println("wrong material Binding"); 
	}
	break;
	case 3:// per part indexed
	case 5:// per face indexed
	{		for (int i=0;i<faceCount;i++){
				fColors[i]=state.diffuse[(colorIndex[0][i])];
				}
			ifsf.setFaceColors(fColors);
			break;
	}
	case 6:// per Vertex
	{
		if (state.diffuse.length>=faceCount){
		int m=0;
		for (int i=0;i<faceCount;i++){
//			int k=faceCount-i-1;
			int faceLength=coordIndex[i].length;
			for (int j=0;j<faceLength;j++){
//				int l=faceLength-1-j;
				vColors[coordIndex[i][j]]=state.diffuse[m];
				m++;
			}
		}
		ifsf.setVertexColors(vColors);
		break;}
	}
		break;
	case 7:// per Vertex indexed 
	{
			for (int i=0;i<faceCount;i++){
//			int k=faceCount-i-1;
			int faceLength=coordIndex[i].length;
			for (int j=0;j<faceLength;j++){
//				int l=faceLength-1-j;
				vColors[coordIndex[i][j]]=state.diffuse[colorIndex[i][j]];
			}
		}
	}
		break;
		default:
		break;
	}
}
	/**
	 * Setzt FarbListe in das gegebene PointSet
	 * @param psf
	 * @param colorIndex Farb-Indizierung
	 * 	zur FarbListe in state 
	 * @param state
	 */
	public static void setColors(PointSet ps,State state,int start,int numP){
	double[][] vColors=new double[numP][3];
	switch (state.materialBinding) {
	case 0:// default
	case 1:// overall
	break;
	case 2:// per part
	case 4:// per face 
	case 6:// per Vertex 
	{
		for (int i=0;i<numP;i++){
			Color c=state.diffuse[i];
			vColors[i][0]=((double)c.getRed())/256;
			vColors[i][1]=((double)c.getGreen())/256;
			vColors[i][2]=((double)c.getBlue())/256;
		}	
	ps.setVertexAttributes(Attribute.COLORS,new DoubleArrayArray.Array(vColors));
	}
	break;
	case 3:// per part indexed
	case 5:// per face indexed
	case 7:// per Vertex indexed 
	{	
		for (int i=start;i<start+numP;i++){
			Color c=state.diffuse[i];
			vColors[i-start][0]=((double)c.getRed())/256;
			vColors[i-start][1]=((double)c.getGreen())/256;
			vColors[i-start][2]=((double)c.getBlue())/256;
		}	
	ps.setVertexAttributes(Attribute.COLORS,new DoubleArrayArray.Array(vColors));
	}
	break;
		default:
		break;
	}
}
	/**
	 * Setzt FarbListe in die gegebene Factory
	 * @param ilsf
	 * @param coordIndex Koordinaten-Indizierung
	 * 	zur KoordinatenListe in state
	 * @param colorIndex Farb-Indizierung
	 * 	zur FarbListe in state 
	 * @param state
	 */
	public static void setColors(IndexedLineSetFactory ilsf,
			int [][] coordIndex,int[][] colorIndex,State state){
	int edgeCount=coordIndex.length;
	int VertexCount= state.coords.length; 
	Color[] eColors=new Color[edgeCount];
	Color[] vColors=new Color[VertexCount];
	for(int i=0;i<VertexCount;i++)
		vColors[i]=Color.BLACK;
	for(int i=0;i<edgeCount;i++)
		eColors[i]=Color.BLACK;
	switch (state.materialBinding) {
	case 0:// default
	case 1:// overall
	break;
	case 2:// per part
	{	System.arraycopy(state.diffuse,0,eColors,0,edgeCount);
		ilsf.setEdgeColors(eColors);
	}
	break;
	case 4:// per face 
		//TODO: get nicht, mache angepasstes "per Vertex"
	{	int m=0;
		for (int i=0;i<edgeCount;i++){
			int edgeLength=coordIndex[i].length;
			for (int j=0;j<edgeLength;j++){
				if (j==edgeLength-1)
					vColors[coordIndex[i][j]]=state.diffuse[m-1];
				else
					vColors[coordIndex[i][j]]=state.diffuse[m];
				m++;
			}
		}
		ilsf.setVertexColors(vColors);
	}
	break;
	case 3:// per part indexed
	{
		for (int i=0;i<edgeCount;i++){
		eColors[i]=state.diffuse[(colorIndex[0][i])];
		}
	ilsf.setEdgeColors(eColors);
	}
	break;
	case 5:// per face indexed
	//TODO: get nicht mache angepasstes "per vertex indexed"
		{
		for (int i=0;i<edgeCount;i++){
			int edgeLength=coordIndex[i].length;
			for (int j=0;j<edgeLength;j++){
				if (j==edgeLength-1)
					vColors[coordIndex[i][j]]=state.diffuse[colorIndex[i][j-1]];
				else
					vColors[coordIndex[i][j]]=state.diffuse[colorIndex[i][j]];
			}
		}
		ilsf.setVertexColors(vColors);
	}
	break;
	case 6:// per Vertex 
		//TODO: eine Farbe pro Punkt! weitere Farben gehen verloren
	{
		int m=0;
		for (int i=0;i<edgeCount;i++){
			int edgeLength=coordIndex[i].length;
			for (int j=0;j<edgeLength;j++){
				vColors[coordIndex[i][j]]=state.diffuse[m];
				m++;
			}
		}
		ilsf.setVertexColors(vColors);
	}
		break;
	case 7:// per Vertex indexed 
	{
		for (int i=0;i<edgeCount;i++){
			int edgeLength=coordIndex[i].length;
			for (int j=0;j<edgeLength;j++){
				vColors[coordIndex[i][j]]=state.diffuse[colorIndex[i][j]];
			}
		}
		ilsf.setVertexColors(vColors);
	}
		break;
		default:
		break;
	}
}
	/**
	 * wandelt eine Int- oder HexZahl 
	 * (gegeben als String) die eine mehrdimensionale Farbe
	 * 	repraesentiert in einen Int-Array i um. 
	 *  Dieser hat je Werte von 0 bis 255. 
	 * @param dim Dimension der Farbe
	 * @param s   String der die Zahl beschreibt
	 * @return    i
	 * Bsp: (4,"0xF001FF") -> ([0,240,1,255])
	 * Bsp: (5,"257")	   -> ([0,0,0,1,1])
	 */
	public static int[]decodeColorFromString(int dim,String s){
		int[] c= new int[dim];
		long dec = Long.decode(s);// to int
		for (int i=0;i<dim;i++){
			int t= (int)(dec%256);
			c[dim-i-1]=t;
			dec -= t;
			dec /= 256;
		}
		return c;
	}
	/**
	 * merges colors by maximum of components
	 * @param c1
	 * @param c2
	 */
	public static Color mergeColor(Color c1, Color c2){
		float r,g,b;
		r= Math.max(c1.getRed(),c2.getRed());
		g= Math.max(c1.getGreen(),c2.getGreen());
		b= Math.max(c1.getBlue(),c2.getBlue());
		return new Color(r,g,b);
	}
	/** appearance set to the root
	 * @return appearance
	 */
	public static Appearance defaultApp(){
		Appearance a= new Appearance();
		a.setAttribute(CommonAttributes.TUBES_DRAW, true);
		a.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		return a;
	}
		
	/**
	 * Changes the coords-List and FaceIndices. After that, every Point is unicly
	 * refferenced by the Faces.
	 * A possible, but not canonical, RefferenceTable will be returnd for changing
	 * Vertex-Attributes-Lists. 
	 * "refferenceTable: [0,..,new size] -> [o,..,old size]
	 * 					 new indice     |->  old Indice " 
	 * @param faces
	 * @param coords
	 * @return a possible refferenceTable
	 */
	public static int[] separateVerticesAndVNormals(int[][] faces, State state){
		int faceC=faces.length;
		int totalVC=0;
		for (int i=0;i<faceC;i++)	totalVC+=faces[i].length;
		double [][] newCoords=new double[totalVC][3];
		double [][] newTexCoords=new double[totalVC][2];
		double [][] newVNormals=new double[totalVC][3];
		int count=0;
		int[] refferenceTable= new int[totalVC];
		for(int f=0;f<faceC;f++){
			for(int v=0;v<faces[f].length;v++){
				if(state.normalBinding==PER_VERTEX){
					newVNormals[count]=new double []{
							state.normals[faces[f][v]][0],
							state.normals[faces[f][v]][1],
							state.normals[faces[f][v]][2]};	
				}
				newCoords[count]=new double []{
					state.coords[faces[f][v]][0],
					state.coords[faces[f][v]][1],
					state.coords[faces[f][v]][2]};
				if(state.textureCoords!=null &&
						state.textureCoords.length==state.coords.length)
					newTexCoords[count]=new double[]{
						state.textureCoords[faces[f][v]][0],
						state.textureCoords[faces[f][v]][1] };
				refferenceTable[count]=faces[f][v];
				faces[f][v]=count;
				count++;
			}
		}
		if(state.textureCoords!=null &&
				state.textureCoords.length==state.coords.length)
			state.textureCoords=newTexCoords;
		state.coords=newCoords;
		if(state.normalBinding==PER_VERTEX){
			state.normals=newVNormals;
		}
		return refferenceTable;
	}
}
