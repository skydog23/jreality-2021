package de.jreality.jogl3.geom;

import java.util.HashMap;
import java.util.Set;

import javax.media.opengl.GL3;

import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.GLVBOInt;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.event.GeometryEvent;

public class JOGLFaceSetEntity extends JOGLLineSetEntity {

	//private GLVBOFloat normalVBO = null;
	private HashMap<String, GLVBO> vbos = new HashMap<String, GLVBO>();
	
	public int getNumVBOs(){
		return vbos.size();
	}
	public GLVBO getVBO(String s){
		return vbos.get(s);
	}
	public GLVBO[] getAllVBOs(){
		GLVBO[] ret = new GLVBO[vbos.size()];
		Set<String> keys = vbos.keySet();
		int i = 0;
		for(String s : keys){
			ret[i] = vbos.get(s);
			i++;
		}
		return ret;
	}
	
	public JOGLFaceSetEntity(IndexedFaceSet node) {
		super(node);
	}
	
	@Override
	public void geometryChanged(GeometryEvent ev) {
		
//		System.out.println("JOGLFaceSetEntity.geometryChanged()");
		super.geometryChanged(ev);
	}

	
	
	//replace state to gl
	public void updateData(GL3 gl) {
		//if (!dataUpToDate) {
			super.updateData(gl);
			vbos.clear();
			IndexedFaceSet fs = (IndexedFaceSet)getNode();
			//create triangulation and save in indexArray
			int count = 0;
			for(int i = 0; i < fs.getNumFaces(); i++){
				IntArray face = fs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
				count += (face.getLength()-2)*3;
			}
			//the array to hold the vertex indices of triangles
			int[] indexArray = new int[count];
			count = 0;
			for(int i = 0; i < fs.getNumFaces(); i++){
				IntArray face = fs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
				
				for(int j = 0; j < (face.getLength()-2); j++){
					indexArray[count + 3*j+0] = face.getValueAt(0);
					indexArray[count + 3*j+1] = face.getValueAt(j+1);
					indexArray[count + 3*j+2] = face.getValueAt(j+2);
				}
				count += (face.getLength()-2)*3;
			}
			//now read all the other attributes
			//first face attributes
			Set<Attribute> aS = fs.getFaceAttributes().storedAttributes();
			for(Attribute a : aS){
				String shaderName = "";
				for(String s : a.getName().split(" ")){
					shaderName = shaderName + s;
				}
				//skip indices, already done earlier
				if(shaderName.equals("indices"))
					continue;
//				System.out.println("!!!!!!!!!!updating face att");
				DataList attribs = fs.getFaceAttributes(a);
				if(isDoubleArrayArray(attribs.getStorageModel())){
					//the array containing one item per index
					double[] inflatedAttributeArray = new double[indexArray.length*4];
					count = 0;
					for(int i = 0; i < fs.getNumFaces(); i++){
						DoubleArray dA = (DoubleArray)attribs.get(i);
						//the face for which the attribute works (not decomposed into triangles)
						IntArray face = fs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
						//for each triangle generated by the face
						for(int j = 0; j < (face.getLength()-2); j++){
							//write the constant face attribute
							//position of the three triangle points
							//in the index array are
							//count + 3*j+0
							//count + 3*j+1
							//count + 3*j+2
							//position in the inflatedArray are
							//(count + 3*j+0)*4
							//(count + 3*j+1)*4
							//(count + 3*j+2)*4
							//now for each point of the triangle
							for(int k = 0; k < 3; k++){
								//for each component of the attribute vector
								inflatedAttributeArray[(count+3*j+k)*4+0] = dA.getValueAt(0);
								if(dA.size() > 1)
									inflatedAttributeArray[(count+3*j+k)*4+1] = dA.getValueAt(1);
								else
									inflatedAttributeArray[(count+3*j+k)*4+1] = 0;
								if(dA.size() > 2)
									inflatedAttributeArray[(count+3*j+k)*4+2] = dA.getValueAt(2);
								else
									inflatedAttributeArray[(count+3*j+k)*4+2] = 0;
								if(dA.size() > 3)
									inflatedAttributeArray[(count+3*j+k)*4+3] = dA.getValueAt(3);
								else
									inflatedAttributeArray[(count+3*j+k)*4+3] = 1;
							}
						}
						count += (face.getLength()-2)*3;
					}
					vbos.put("face_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "face_"+a.getName()));
					//vbos.add(new GLVBOFloat(state.getGL(), Rn.convertDoubleToFloatArray(inflatedAttributeArray), "face_"+a.getName()));
					System.out.println("creating " + "face_"+a.getName());
				}else if(isIntArray(attribs.getStorageModel())){

					//the array containing one item per index
					int[] inflatedAttributeArray = new int[indexArray.length*4];
					count = 0;
					for(int i = 0; i < fs.getNumFaces(); i++){
						IntArray dA = (IntArray)attribs.get(i);
						//the face for which the attribute works (not decomposed into triangles)
						IntArray face = fs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
						//for each triangle generated by the face
						for(int j = 0; j < (face.getLength()-2); j++){
							//write the constant face attribute
							//position of the three triangle points
							//in the index array are
							//count + 3*j+0
							//count + 3*j+1
							//count + 3*j+2
							//position in the inflatedArray are
							//(count + 3*j+0)*4
							//(count + 3*j+1)*4
							//(count + 3*j+2)*4
							//now for each point of the triangle
							for(int k = 0; k < 3; k++){
								//for each component of the attribute vector
								inflatedAttributeArray[(count+3*j+k)*4+0] = dA.getValueAt(0);
								if(dA.size() > 1)
									inflatedAttributeArray[(count+3*j+k)*4+1] = dA.getValueAt(1);
								else
									inflatedAttributeArray[(count+3*j+k)*4+1] = 0;
								if(dA.size() > 2)
									inflatedAttributeArray[(count+3*j+k)*4+2] = dA.getValueAt(2);
								else
									inflatedAttributeArray[(count+3*j+k)*4+2] = 0;
								if(dA.size() > 3)
									inflatedAttributeArray[(count+3*j+k)*4+3] = dA.getValueAt(3);
								else
									inflatedAttributeArray[(count+3*j+k)*4+3] = 1;
							}
						}
						count += (face.getLength()-2)*3;
					}
					vbos.put("face_"+shaderName, new GLVBOInt(gl, inflatedAttributeArray, "face_"+a.getName()));
					System.out.println("creating " + "face_"+a.getName());
				}else{
					System.out.println("FSE 1: not knowing what to do with " + attribs.getStorageModel().toString());
				}
				//System.out.println("face attribute names: " + a.getName());
			}
			
			
			//then comes vertex attributes
			aS = fs.getVertexAttributes().storedAttributes();
			for(Attribute a : aS){
				String shaderName = "";
				for(String s : a.getName().split(" ")){
					shaderName = shaderName + s;
				}
				DataList attribs = fs.getVertexAttributes(a);
				if(isDoubleArray(attribs.getStorageModel())){
					//the array containing one item per index
					double[] inflatedAttributeArray = new double[indexArray.length];
					//count = 0;
					//for each index in the indexArray
					for(int i = 0; i < indexArray.length; i++){
						//we retrieve the vertex attribute
						int j = indexArray[i];
						DoubleArray dA = (DoubleArray)attribs;
						
						inflatedAttributeArray[i] = dA.getValueAt(j);
					}
					vbos.put("vertex_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "vertex_"+a.getName(), 1));
					System.out.println("creating " + "vertex_"+a.getName());
				}else if(isDoubleArrayArray(attribs.getStorageModel())){
					//the array containing one item per index
					double[] inflatedAttributeArray = new double[indexArray.length*4];
					//count = 0;
					//for each index in the indexArray
					for(int i = 0; i < indexArray.length; i++){
						//we retrieve the vertex attribute
						int j = indexArray[i];
						DoubleArray dA = (DoubleArray)attribs.get(j);
						
						inflatedAttributeArray[4*i+0] = dA.getValueAt(0);
						if(dA.size() > 1)
							inflatedAttributeArray[4*i+1] = dA.getValueAt(1);
						else
							inflatedAttributeArray[4*i+1] = 0;
						if(dA.size() > 2)
							inflatedAttributeArray[4*i+2] = dA.getValueAt(2);
						else
							inflatedAttributeArray[4*i+2] = 0;
						if(dA.size() > 3)
							inflatedAttributeArray[4*i+3] = dA.getValueAt(3);
						else
							inflatedAttributeArray[4*i+3] = 1;
					}
					
					vbos.put("vertex_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "vertex_"+a.getName()));
					System.out.println("creating " + "vertex_"+a.getName());
				
				}else if(isIntArray(attribs.getStorageModel())){
					//the array containing one item per index
					int[] inflatedAttributeArray = new int[indexArray.length*4];
					//count = 0;
					//for each index in the indexArray
					for(int i = 0; i < indexArray.length; i++){
						//we retrieve the vertex attribute
						int j = indexArray[i];
						IntArray dA = (IntArray)attribs.get(j);
						
						inflatedAttributeArray[4*i+0] = dA.getValueAt(0);
						if(dA.size() > 1)
							inflatedAttributeArray[4*i+1] = dA.getValueAt(1);
						else
							inflatedAttributeArray[4*i+1] = 0;
						if(dA.size() > 2)
							inflatedAttributeArray[4*i+2] = dA.getValueAt(2);
						else
							inflatedAttributeArray[4*i+2] = 0;
						if(dA.size() > 3)
							inflatedAttributeArray[4*i+3] = dA.getValueAt(3);
						else
							inflatedAttributeArray[4*i+3] = 1;
					}
					vbos.put("vertex_"+ shaderName, new GLVBOInt(gl, inflatedAttributeArray, "vertex_"+a.getName()));
					System.out.println("creating " + "vertex_"+a.getName());
				
				}else{
					System.out.println("FSE2: not knowing what to do with " + attribs.getStorageModel().toString() + ", " + a.getName());
				}
				//System.out.println("face attribute names: " + a.getName());
			}
			
			//and finally edge attributes
			aS = fs.getEdgeAttributes().storedAttributes();
			for(Attribute a : aS){
//				System.out.println(a.getName());
				//TODO
			}
			
			
//			aS = fs.getFaceAttributes().storedAttributes();
//			for(Attribute a : aS){
//				System.out.println("face attribute names: " + a.getName());
//			}
//			aS = fs.getVertexAttributes().storedAttributes();
//			for(Attribute a : aS){
//				System.out.println("vertex attribute names: " + a.getName());
//			}
//			aS = fs.getEdgeAttributes().storedAttributes();
//			for(Attribute a : aS){
//				System.out.println("edge attribute names: " + a.getName());
//			}
			/*
			indexVBO = new GLVBOInt(state.getGL(), indexArray, "indices");
			
			DataList vA = fs.getVertexAttributes(Attribute.NORMALS);
			double[] normaldata = new double[fs.getNumPoints()*3];
			if(vA != null){
				vA.toDoubleArray(normaldata);
			}
			
			normalVBO = new GLVBOFloat(state.getGL(), Rn.convertDoubleToFloatArray(normaldata), "vertex.normals");
			*/
			//dataUpToDate = true;
			
//			System.out.println("data up to date: "+getNode().getName());
			
		//}
	}
}
