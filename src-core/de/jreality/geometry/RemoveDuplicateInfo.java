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

/** 
 * @author Bernd Gonska
 */
package de.jreality.geometry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.StringArrayArray;

public class RemoveDuplicateInfo {
	

	/** retains only vertices which differs enough in the given
	 * attributes. 
	 * <i>enough</i> means the euclidean distance is smaler than <code>eps</code> 
	 * retains only the standard Vertex Attributes.
	 * face- and edge- attributes stay the same.
	 * only Face and Edge Indices changes.
	 * 
	 * Remark: The GeonmetryAttribute 
	 * 			<code>quadmesh</code> will be deleted
	 * Remark: some other Attributes may collide with the new Geometry
	 * 
	 * @param ps       can be <code>IndexedFaceSet,IndexedLineSet or PointSet</code>
	 * @param atts	   some <code>doubleArrayArrayAttributes</code> 
	 * @return IndexedFaceSet  
	 */
////---------- new start-----------------
	private int[] RefferenceTable;
	private int[] mergeRefferenceTable;
	private int[] removeRefferenceTable;
	private int[] sublistTable;
	
	private IndexedFaceSet source;
	private IndexedFaceSet geo= new IndexedFaceSet();
	private double[][] points; // the vertices
	
	private double eps; // Tolereanz for merging
	private int dim;// =points[x].length
	private int maxPointPerBoxCount=50;
	private int numSubBoxes;
	private int numNewVerts;
	// constr ----------------------------------------
	private RemoveDuplicateInfo(IndexedFaceSet ifs, Attribute ...attributes ){
		source=ifs;
		points=source.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//TODO: handle  Attributes
	}
	// methods----------------------------------------
	public static IndexedFaceSet removeDuplicateVertices(IndexedFaceSet ps, Attribute ...attributes ) {
		return removeDuplicateVertices(ps,0.00000000001,attributes);		
	}
	public static IndexedFaceSet removeDuplicateVertices(IndexedFaceSet ps, double eps, Attribute ...attributes ) {
		IndexedFaceSet ifs= IndexedFaceSetUtility.pointSetToIndexedFaceSet(ps);		
		// inittialize some data
		RemoveDuplicateInfo r= new RemoveDuplicateInfo(ifs);
		{
			// TODO: make better output
			if(r.points.length==0) return null;
			if(r.points.length==1) return null;
		}
		//
		r.dim=r.points[0].length;
		r.mergeRefferenceTable=new int[r.points.length];
		for (int i = 0; i < r.points.length; i++) {
			r.mergeRefferenceTable[i]=i;
		}
		r.numSubBoxes=(int)Math.pow(2, r.dim);
		// create first box:
		Box b=r.fillFirstBox();
		// start sortBox(firstBox):
		r.processBox(b);
		// return:
		r.postCalulation();
		return r.geo;
	} 
	
	private Box fillFirstBox(){// finished
		double[] max= new double[dim];
		double[] min= new double[dim];
		for (int i = 0; i < dim; i++) 
			min[i]=max[i]=points[0][i];
		for (int i = 1; i < points.length; i++) {
			for (int j = 0; j < dim; j++) {
				double[] p=points[i];
				if(p[j]>=max[j]) max[j]=p[j];
				if(p[j]<=min[j]) min[j]=p[j];
			}	
		}
		Box b=new Box(max,min,dim);
		for (int i = 0; i < points.length; i++) {
			b.addPointIfPossible(i);
		}
		return b;
	}
	
	
/** fills the refferences 
 * of the Vertices in the Box
 * in the refferenceTable
 */	
	private void processBox(Box b){// finished
		if(b.numOfPoints==0) return;
		// case of to small Box:
		if(b.getSize()<3*eps) {
			mergeAllToOne(b);
			return;
		}
		// recursion case(subdivision needed):
		if(b.numOfPoints>maxPointPerBoxCount){
			Box[] subBoxes = createSubBoxes(b);
			for (int i = 0; i < subBoxes.length; i++) {
				processBox(subBoxes[i]);
			}
			return;
		}
		// comparing:
		compareInBox(b);
	}
	/** indicates if a Point is within the box given by 
	 *  min and max plus an eps.
	 * 
	 */
	private boolean inBetwen(double[]max,double[]min,double eps, double[] val){// finished
		for (int i = 0; i < val.length; i++) {
			if(val[i]>max[i]+eps) return false;
			if(val[i]<min[i]-eps) return false;
		}
		return true;
	}
	/** sets the refferences of all Vertices in the box	
	 */ 
	private void compareInBox(Box b) {// finished
		for (int i: b.innerPoints){
			if(!isLegalPoint(i)) continue;
			for (int j: b.innerPoints){
				if(i>=j)continue;
				if(!isLegalPoint(j)) continue;
				double[] p1=points[i];
				double[] p2=points[j];
				if (inBetwen(p1, p1, eps, p2))
					mergeRefferenceTable[j]=i;
			}
		}
	}
	private void mergeAllToOne(Box b) {// finished
		int dest=b.innerPoints.get(0);
		for (int p : b.innerPoints) {
			mergeRefferenceTable[p]=dest;
		}
	}
	/** indicates if a point is not refferenced to an other;
	 * @param p
	 * @return
	 */
	private boolean isLegalPoint(int p){// finished
		return (mergeRefferenceTable[p]==p);
	}
	
	private Box[] createSubBoxes(Box b) {// finished 
		Box[] result= new Box[numSubBoxes];
		for (int i = 0; i < result.length; i++) {
			// calc max & min:
			double[] min= new double[dim];
			double[] max= new double[dim];
			int k=i;
			for (int d = 0; d < dim; d++) {
				if(k%2==0){
					if(b.realMax[d]-b.realMin[d]<=2*eps){
						max[d]=min[d]=(b.realMax[d]+b.realMin[d])/2;
					}
					else{	
						max[d]=(b.originalMin[d]+b.originalMax[d])/2;
						min[d]=b.originalMin[d]+eps;
					}
				}
				else{
					min[d]=(b.originalMin[d]+b.originalMax[d])/2;
					max[d]=b.originalMax[d];
				}
				k = k>>1;
			}
			// make new subBox
			result[i]=new Box(max,min,dim);
		}
		// insert points
		for(int v: b.innerPoints)
			if(isLegalPoint(v))
				for (int i = 0; i < numSubBoxes; i++) 
					// bei allen probieren 
					result[i].addPointIfPossible(v);
		return result;
	}
	// DataStructures----------------------------------------
	/** holds a bucket full of points.
	 *  this points will be directly compared (O(n^2)) 
	 */
	private class Box{
		int numOfPoints=0; 
		double[] originalMax; // without eps
		double[] originalMin; // without eps
		double[] realMax;
		double[] realMin;
		boolean empty;
		List<Integer> innerPoints= new LinkedList<Integer>();
		/** box with boundarys,
		 *  can be filled with (double[dim]) Points 
		 */
		public Box(double[]max,double[]min,int dim) {// finished
			originalMax=max;
			originalMin=min;
			realMax=new double[dim];
			realMin=new double[dim];
			empty=true;
		}
		/** returnes if a point can be added
		 * (lies within the boundary)
		 */
		public boolean addable(int d){// finished
			double[] p=points[d];
			return inBetwen(originalMax, originalMin, eps, p);
		}
		/** adds a Point to the box if possible 
		 *  updates the real bounding
		 *  returns succes
		 */
		public boolean addPointIfPossible(int point){// finished
			if(!addable(point))return false;
			double[] p=points[point];
			if (empty){
				for (int i = 0; i < dim; i++) {
					realMax[i]=realMin[i]=p[i];
				}
				empty=false;
			}
			else{
				for (int i = 0; i < dim; i++) {
					if(p[i]>=realMax[i]) realMax[i]=p[i];
					if(p[i]<=realMin[i]) realMin[i]=p[i];
				}
			}
			innerPoints.add(point);
			numOfPoints++;
			return true;
		}
		double getSize(){
			double size=0;
			for (int i = 0; i < dim; i++) 
				if(realMax[i]-realMin[i]>size)
					size=realMax[i]-realMin[i];
			return size;
		}
	}
	
// post calculation -------------------------------------------------------
	
	private void postCalulation(){
		newTables();
		geo.setNumPoints(numNewVerts);		
		geo.setNumFaces(source.getNumFaces());
		geo.setNumEdges(source.getNumEdges());
		newDatalists();
		newIndices();
	} 
	/** calculates refferenceTable 
	 * new Vertices 
	 * (unused Vertices will be taken out) 
	 */
	private void newTables(){
		// remove Table:
		removeRefferenceTable= new int[points.length];
		int numUsedVerts=0;
		int pos=0;
		for (int i = 0; i < points.length; i++)
			if (mergeRefferenceTable[i]==i){
				removeRefferenceTable[i]=numUsedVerts;
				numUsedVerts++;
			}
			else{
				removeRefferenceTable[i]=-1;
			}
		// direct referenceTable:
		RefferenceTable= new int[points.length];
		for (int i = 0; i < points.length; i++) {
			RefferenceTable[i]=removeRefferenceTable[mergeRefferenceTable[i]];
		}
		numNewVerts=numUsedVerts;
		// sublist Table:
		sublistTable= new int[numUsedVerts];
		pos=0;
		for (int i = 0; i < points.length; i++) 
			if(removeRefferenceTable[i]!=-1){
				sublistTable[pos]=i;
				pos++;
			}
	}
	
	/** 
	 * @param oldRefferences (for ecx.: face indices)
	 * @param refferenceTable (result of start)
	 * @return new refferences (for ecx.: new face indices)
	 */
	private void newIndices(){
		// face Indices
		DataList data=source.getFaceAttributes(Attribute.INDICES);
		if(data!=null && data.size()>0 ){
			int[][] fIndis=data.toIntArrayArray(null);
			int[][] result= new int[fIndis.length][];
			for (int i = 0; i < result.length; i++) {
				result[i]=newIndices(fIndis[i], RefferenceTable);
			}
			geo.setFaceAttributes(Attribute.INDICES,new IntArrayArray.Array(result));
		}
		// edge Indices
		data=source.getEdgeAttributes(Attribute.INDICES);
		if(data!=null && data.size()>0 ){
			int[][] eIndis=data.toIntArrayArray(null);
			int[][] result= new int[eIndis.length][];
			for (int i = 0; i < result.length; i++) {
				result[i]=newIndices(eIndis[i], RefferenceTable);
			}
			geo.setEdgeAttributes(Attribute.INDICES,new IntArrayArray.Array(result));
		}
	}
	private static int[] newIndices(int[] oldRefferences, int[] refferenceTable){
		int[] result= new int[oldRefferences.length];
		for (int i = 0; i < oldRefferences.length; i++) {
			result[i]=refferenceTable[oldRefferences[i]];
		}
		return result;
	} 
	private void newDatalists() {
		DataListSet datas=source.getVertexAttributes();
		Set<Attribute> atts=(Set<Attribute>) datas.storedAttributes();
		// VertexDatalists
		for(Attribute at : atts){
			DataList dl=datas.getList(at);
			if (dl instanceof DoubleArrayArray) {DoubleArrayArray dd = (DoubleArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof DoubleArray) {DoubleArray dd = (DoubleArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof IntArrayArray) {IntArrayArray dd = (IntArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof IntArray) {IntArray dd = (IntArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof StringArrayArray) {
				StringArrayArray dd = (StringArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof StringArray) {StringArray dd = (StringArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
		}
		geo.setEdgeAttributes(source.getEdgeAttributes());
		geo.setFaceAttributes(source.getFaceAttributes());
		geo.setGeometryAttributes(source.getGeometryAttributes());
		geo.setGeometryAttributes("quadMesh",null);
	}
	// getter / setter ---------------------------------------- 
	/** get Tolerance for merging*/
	public double getEps() {
		return eps;
	}
	/** set Tolerance for merging*/
	public void setEps(double eps) {
		this.eps = eps;
	}
	public int[] getRefferenceTable() {
		return RefferenceTable;
	}
////---------- new end ------------------
	
	
//	public static IndexedFaceSet removeDuplicateVertices(PointSet ps, Attribute ... atts){
//		
//		
//		List<Attribute> attrs=new LinkedList<Attribute>();
//		for (int i = 0; i < atts.length; i++) {
//			attrs.add(atts[i]);
//		}
//		if(!attrs.contains(Attribute.COORDINATES))// Koordinaten muessen dabei sein!
//			attrs.add(Attribute.COORDINATES);
//		int numOfVertices	=ifs.getNumPoints();
//
//		// compareData [Attr][Vertex][dim]
//		List<double[][]> compareDataTemp = new LinkedList<double[][]>();
//		List<Attribute> goodAttrs = new LinkedList<Attribute>();
//
//		//compareData auslesen und nur funktionierende Attribute merken:
//		int totalDim=0;			// gesammelte dimension der zu vergl. Attribute
//		for(Attribute a:attrs){
//			try {
//				double[][] temp=ifs.getVertexAttributes (a).toDoubleArrayArray(null);
//				int dim=temp[0].length;
//				compareDataTemp.add(temp);
//				totalDim+=dim;
//				goodAttrs.add(a);
//			}catch (Exception e) {}
//		}
//		int numOfAttr=goodAttrs.size();
//		// compareData[vertex][attr][dim]
//		double[][][] compareData = new double[numOfVertices][numOfAttr][];
//		for (int i = 0; i < numOfAttr; i++) { // change sizing
//			for (int j = 0; j < numOfVertices; j++) {
//				compareData[j][i]= compareDataTemp.get(i)[j];
//			}
//		}
//
//		// die alten Daten auslesen	
//		int[][]    oldVertexIndizeesArray=null;
//		String[]   oldVertexLabelsArray=null;
//
//		DataList temp;
//		temp=ifs.getVertexAttributes ( Attribute.INDICES );
//		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
//		temp= ifs.getVertexAttributes( Attribute.LABELS );
//		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
//
//		// anders regeln!!! <<=>---<<<
//		double [][] oldVertexCoordsArray=null;
//		double[][] oldVertexColorArray=null;
//		double[][] oldVertexNormalsArray=null;
//		double[]   oldVertexSizeArray= null;
//		double[][] oldVertexTextureCoordsArray=null;
//		temp= ifs.getVertexAttributes( Attribute.NORMALS );
//		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
//		temp= ifs.getVertexAttributes( Attribute.POINT_SIZE);
//		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
//		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
//		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
//		temp= ifs.getVertexAttributes ( Attribute.COORDINATES );
//		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
//		temp= ifs.getVertexAttributes ( Attribute.COLORS );
//		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
//
//
//		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
//		int[] refferenceTabel =new int[numOfVertices];
//
//		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
//		// neue Attribute der Punkte zwischenspeichern:
//		int curr=0; // : aktuell einzufuegender Index 
//		int index;
//		DimTreeStart dTree=new RemoveDuplicateInfo().new DimTreeStart(totalDim);
//
//		if (numOfVertices>0){
//			for (int i=0; i<numOfVertices;i++){
//				// Trick :benutze durchgelaufenen Teil der Datenliste fuer neue Daten
//				index=dTree.put(compareData[i]);	// pruefe ob Vertex doppelt 
//				refferenceTabel[i]=index; //Indizes vermerken 
//				if(curr==index){
//					// nur notwendige Daten uebertragen: 
//					oldVertexCoordsArray[curr]=oldVertexCoordsArray[i];
//					if (oldVertexColorArray!=null)
//						oldVertexColorArray[curr]=oldVertexColorArray[i];
//					if (oldVertexIndizeesArray!=null)
//						oldVertexIndizeesArray[curr]=oldVertexIndizeesArray[i];
//					if (oldVertexLabelsArray!=null)
//						oldVertexLabelsArray[curr]=oldVertexLabelsArray[i];
//					if (oldVertexNormalsArray!=null)
//						oldVertexNormalsArray[curr]=oldVertexNormalsArray[i];
//					if (oldVertexSizeArray!=null)
//						oldVertexSizeArray[curr]=oldVertexSizeArray[i];
//					if (oldVertexTextureCoordsArray!=null)
//						oldVertexTextureCoordsArray[curr]=oldVertexTextureCoordsArray[i];
//					curr++;
//					System.out
//							.println("RemoveDublicateInfo.removeDublicateVertices(curr)"+curr);
//				}
//			}	
//		}
//		int numOfVerticesNew = curr;
//
//		// Die VertexAttributVektoren kuerzen		
//		double[][] newVertexColorArray= 		new double[numOfVerticesNew][];
//		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][];
//		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
//		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][];
//		double[][] newVertexTextureCoordsArray= new double[numOfVerticesNew][];
//		double[]   newVertexSizeArray= 			new double[numOfVerticesNew];
//		int[][]    newVertexIndizeesArray= 		new int[numOfVerticesNew][];
//
//		for(int i=0;i<numOfVerticesNew;i++){
//			if (oldVertexCoordsArray!=null)			newVertexCoordsArray[i]=oldVertexCoordsArray[i];
//			if (oldVertexColorArray!=null)			newVertexColorArray[i]=oldVertexColorArray[i];
//			if (oldVertexIndizeesArray!=null)		newVertexIndizeesArray[i]=oldVertexIndizeesArray[i];
//			if (oldVertexLabelsArray!=null)			newVertexLabelsArray[i]=oldVertexLabelsArray[i];
//			if (oldVertexNormalsArray!=null)		newVertexNormalsArray[i]=oldVertexNormalsArray[i];
//			if (oldVertexSizeArray!=null)			newVertexSizeArray[i]=oldVertexSizeArray[i];
//			if (oldVertexTextureCoordsArray!=null)	newVertexTextureCoordsArray[i]=oldVertexTextureCoordsArray[i];
//		}
//
//		// Die Vertex Attribute wieder einfuegen
//		IndexedFaceSet result=new IndexedFaceSet();
//		result.setNumPoints(numOfVerticesNew);
//
//		if (numOfVerticesNew>0){
//			if (oldVertexCoordsArray!=null){
//				System.out.println("coords");
//				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
//			}
//			if (oldVertexColorArray!=null){
//				System.out.println("color");
//				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
//			}
//			if (oldVertexLabelsArray!=null){
//				System.out.println("labels");
//				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
//			}
//			if (oldVertexNormalsArray!=null){
//				System.out.println("normals");
//				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
//			}
//			if (oldVertexTextureCoordsArray!=null){
//				System.out.println("texture");
//				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
//			}
//			if (oldVertexSizeArray!=null){
//				System.out.println("size");
//				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
//			}
//			if (oldVertexIndizeesArray!=null){
//				System.out.println("indicees");
//				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
//			}
//		}
//
//		// uebernehmen der alten Attribute
//		int numOfEdges		=ifs.getNumEdges();
//		int numOfFaces		=ifs.getNumFaces();
//		result.setNumEdges(numOfEdges);
//		result.setNumFaces(numOfFaces);
//
//		result.setGeometryAttributes(ifs.getGeometryAttributes());
//		result.setEdgeAttributes(ifs.getEdgeAttributes());
//		result.setFaceAttributes(ifs.getFaceAttributes());
//
//		// die Indices angleichen:		
//		int [][] faceIndicesOld=null;
//		int [][] edgeIndicesOld=null;
//		temp=ifs.getFaceAttributes( Attribute.INDICES );
//		if (temp !=null){
//			faceIndicesOld = temp.toIntArrayArray(null);
//			int [][] faceIndicesNew= makeNewIndicees(faceIndicesOld,refferenceTabel);
//			if((numOfFaces>0)&(numOfVertices>0))
//				result.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(faceIndicesNew));
//		}
//		temp=ifs.getEdgeAttributes( Attribute.INDICES );
//		if (temp !=null){
//			edgeIndicesOld = temp.toIntArrayArray(null);
//			int [][] edgesIndicesNew=makeNewIndicees(edgeIndicesOld,refferenceTabel);
//			if((numOfEdges>0)&(numOfVertices>0))
//				result.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgesIndicesNew));
//		}
//		return result;		
//
//	}

	
	/** removes vertices which are not used by faces.
	 * changes faceIndices.
	 * @param vertices
	 * @param faces
	 * @return vertices
	 */
	public static double[][] removeNoFaceVertices(double[][] vertices, int[][] faces){
		int numVOld=vertices.length;
		int numF=faces.length;
		boolean[] usedVertices= new boolean[numVOld];
		for (int i = 0; i < numVOld; i++) 
			usedVertices[i]=false;
		// remember all vertices used in faces
		for (int i = 0; i < numF; i++) 
			for (int j = 0; j < faces[i].length; j++) 
				usedVertices[faces[i][j]]=true;	
		int count=0; 
		int[] refferenceTabel= new int[numVOld];
		for (int i = 0; i < numVOld; i++) {
			if(usedVertices[i]){
				refferenceTabel[i]=count;
				vertices[count]=vertices[i];// vertices gleich richtig einschreiben
				count++;
			}
			else{
				refferenceTabel[i]=-1;
			}
		}
		// faces umindizieren
		for (int i = 0; i < numF; i++) 
			for (int j = 0; j < faces[i].length; j++) 
				faces[i][j]=refferenceTabel[faces[i][j]];
		// VertexListe erneuern
		double[][] newVertices= new double[count][];
		System.arraycopy(vertices, 0, newVertices, 0, count);
		return newVertices;
	}
	/** a face definition can repeat the first index at the end  
	 * excample: {1,2,3,4,1} or {1,2,3,4}
	 * in first case: the last index will be removed
	 */
	public static void removeCycleDefinition(int[][] faces){
		for (int i = 0; i < faces.length; i++) {
			int len=faces[i].length;
			if(len>1)
				if(faces[i][len-1]==faces[i][0]){
					int[] newIndis= new int[len-1];
					System.arraycopy(faces[i], 0, newIndis, 0, len-1);
					faces[i]=newIndis;
				}
		}
	}
	// ------------------ sublists -----------------------------
	public static DataList getSublist(DoubleArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toDoubleArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(double[][] dd, int[] referenceTable){
		if (dd.length==0)return new DoubleArrayArray.Array(new double[][]{{}});
		int dim=dd[0].length;
		double[][] newList=new double[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new DoubleArrayArray.Array(newList);
	} 
	public static DataList getSublist(DoubleArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toDoubleArray(null), referenceTable);
	} 
	public static DataList getSublist(double[] d, int[] referenceTable){
		if (d.length==0)return new DoubleArray(new double[]{});
		double[] newList=new double[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new DoubleArray(newList);
	} 
	public static DataList getSublist(IntArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toIntArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(int[][] dd, int[] referenceTable){
		if (dd.length==0)return new IntArrayArray.Array(new int[][]{{}});
		int dim=dd[0].length;
		int[][] newList=new int[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new IntArrayArray.Array(newList);
	} 
	public static DataList getSublist(IntArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toIntArray(null), referenceTable);
	} 
	public static DataList getSublist(int[] d, int[] referenceTable){
		if (d.length==0)return new IntArray(new int[]{});
		int[] newList=new int[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new IntArray(newList);
	} 
	public static DataList getSublist(StringArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toStringArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(String[][] dd, int[] referenceTable){
		if (dd.length==0)return new StringArrayArray.Array(new String[][]{{}});
		int dim=dd[0].length;
		String[][] newList=new String[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new StringArrayArray.Array(newList);
	} 
	public static DataList getSublist(StringArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toStringArray(null), referenceTable);
	} 
	public static DataList getSublist(String[] d, int[] referenceTable){
		if (d.length==0)return new StringArray(new String[]{});
		String[] newList=new String[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new StringArray(newList);
	} 
	
}
