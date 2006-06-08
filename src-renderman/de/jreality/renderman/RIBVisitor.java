/*
 * Created on 09.02.2004
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.renderman;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Cylinder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;


/**
 * A Visitor for writing renderman<sup>TM</sup> rib files. At the moment the following 
 * things do not work as expected:
 * <ul>
 * <li>twoside shading is not supported</li>
 * <li>object transparency is not multiplied with vertex alpha. the latter will override
 * if present</li>
 * <li>clipping planes are written but Icould not test them since neither 
 * 3delight<sup>TM</sup>  nor aqsis do support them at the moment</li>
 * <li>lots of other stuff I just did not check...</li>
 * <li>...</li>
 * <li>face colors on IndexedFaceSets ignored</li>
 * </ul>
 *  * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class RIBVisitor extends SceneGraphVisitor {
    private int width =640;
    private int height = 480;
    private String name;
    protected EffectiveAppearance eAppearance;
    private int textureCount = 0;
    private Map textures =new HashMap();
    private String proj = "perspective";
    private int[] maximumEyeSplits={10};
    
    public static boolean fullSpotLight = true;
    public static String shaderPath = null;
    
    /**
     * 
     */
    public RIBVisitor() {
        super();
        eAppearance=EffectiveAppearance.create();
    }
    public void visit(SceneGraphComponent root, SceneGraphPath path, String name) {
        //SceneGraphPath path =SceneGraphPath.getFirstPathBetween(root,camera);
        Camera camera =(Camera) path.getLastElement();
        this.name =name;
        double[] cam =path.getInverseMatrix(null);
        try {
            File file = new File(name);
            System.out.println("writing in  "+file);
            file = new File(file.getParent(),"transformedpaintedplastic.sl");
            System.out.println("checking on "+file+" exists "+file.exists());
            if(!file.exists()) {
               // file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            InputStream is = getClass().getResourceAsStream("transformedpaintedplastic.sl");
            
            int c = 0;
            while((c =is.read())!=-1) {
                os.write(c);
////                System.out.print((char)c);
            }
            os.close();
            is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!name.endsWith(".rib"))
            name = name+".rib";
        Ri.begin(name);
        HashMap map = new HashMap();
        map.put("shader", (shaderPath!=null?(shaderPath+":"):"")+".:&");
        //map.put("shader", (fullSpotLight!=null?(fullSpotLight+":"):"")+".:&");
        Ri.option( "searchpath", map);
        
        HashMap map2 = new HashMap();
        map2.put("eyesplits",maximumEyeSplits);
        Ri.option("limits",map2);
        //We ensured that name ends with .rib so :
        String outputName = name.substring(0,name.length()-3)+"tif";
        Ri.display(outputName, "tiff", "rgb",null);
        
        Ri.format(width,height,1);
        System.out.println("ww "+width+" hh "+height);
        Ri.shadingRate(1f);
        map = new HashMap();
        float fov = (float) camera.getFieldOfView();
        if(proj.equals("perspective")) {
            map.put("fov", new Float(fov));
            Ri.projection(proj,map);
        } else {
            Ri.projection(proj,map);    
            float a =(float) (1/((Math.tan((Math.PI/180.0)*camera.getFieldOfView()/2.0)*camera.getFocus())));
            Ri.transform(new float[] {a,0,0,0,0,a,0,0,0,0,1,0,0,0,0,1});
        }
        map = new HashMap();
        Appearance ap = root.getAppearance();
        Color col = Color.WHITE;
        if(ap!=null) { 
            Object o = ap.getAttribute(CommonAttributes.BACKGROUND_COLOR,Color.class);
            if(o instanceof Color) col = (Color) o;
        }
        float[] f = col.getRGBColorComponents(null);
        map.put("color background", f);
        Ri.imager("background",map);
        cam = MatrixBuilder.euclidean().scale(1,1,-1).times(cam).getArray();
        if(proj.equals("perspective"))
            Ri.transform(fTranspose(cam));
        else
            Ri.concatTransform(fTranspose(cam));
        Ri.worldBegin();
        new LightCollector(root);
        root.accept(this);
        Ri.worldEnd();
        Ri.end();
    }
     /**
     * @param cam
     * @return
     */
    private static float[] fTranspose(double[] mat) {
        float[] tmat = new float[16];
        for (int i = 0; i < 4; i++) 
            for (int j = 0;j<4;j++){
                tmat[i + 4*j] = (float) mat[j+4*i];
            }
        return tmat;
    }
    
    public void  visit(SceneGraphComponent c) {
        Ri.comment(c.getName());        
        Ri.attributeBegin();
        EffectiveAppearance tmp =eAppearance;
        Appearance a = c.getAppearance();
        if(a!= null) {
            eAppearance = eAppearance.create(a);
            //FIXME: omit this call to avoid writing attributes twice???
            setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        }
        c.childrenAccept(this);
        Ri.attributeEnd();
        
        eAppearance= tmp;
    }
     public void visit(Transformation t) {
         double[] mat = t.getMatrix();
         float[] tmat = new float[16];
         for (int i = 0; i < 4; i++) 
         	for (int j = 0;j<4;j++){
            tmat[i + 4*j] = (float) mat[j+4*i];
        }
         Ri.concatTransform(tmat);
     }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.Appearance)
     */
    public void visit(Appearance a) {
       /// eAppearance = eAppearance.create(a);
        
        //super.visit(a);
    }

    private void setupShader(EffectiveAppearance a, String type) {
        // Attribute
        Map m = (Map) a.getAttribute("rendermanAttribute",null, Map.class);
        if(m!=null) {
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                Ri.attribute(key,(Map)m.get(key));
            }
        }
        
        Object color = a.getAttribute(type+"."+CommonAttributes.DIFFUSE_COLOR,CommonAttributes.DIFFUSE_COLOR_DEFAULT);
        if(color!=Appearance.INHERITED) {
            float[] c =((Color)color).getComponents(null);
            Ri.color(new float[] {c[0],c[1],c[2]});
        }
    
    double transparency = a.getAttribute(type+"."+CommonAttributes.TRANSPARENCY,CommonAttributes.TRANSPARENCY_DEFAULT);
        float f = 1f - (float)transparency;
        Ri.opacity(new float[] {f,f,f});
        //System.out.println("transparency is "+type+" is "+transparency);
        Object shader = a.getAttribute(type,"default");
        //System.out.println("shader for "+type+" is "+shader);

        SLShader slShader = (SLShader) a.getAttribute(type+".rendermanDisplacement",null,SLShader.class);
        if(slShader != null) {
            Ri.displacement(slShader.getName(),slShader.getParameters());
        }
        slShader = (SLShader) a.getAttribute(type+".rendermanSurface",null,SLShader.class);
        if(slShader == null) {
            if(true || shader.equals("default")) {
                float phongSize =(float) a.getAttribute(type+"."+CommonAttributes.SPECULAR_EXPONENT,CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
                float phong =(float) a.getAttribute(type+"."+CommonAttributes.SPECULAR_COEFFICIENT,CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
                HashMap map =new HashMap(); 
                map.put("roughness",new Float(1/phongSize));
                map.put("Ks",new Float(phong));
                map.put("Kd",new Float(1));
                
                //System.out.println("has texture "+AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), a));
                if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), a)) {
                    Texture2D tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), a);
               //System.out.println("texture is "+tex);
//                Texture2D tex = (Texture2D) a.getAttribute(type+".texture",null,Texture2D.class);
//                if(tex != null) {
                    String fname = writeTexture(tex);
                    map.put("string texturename",fname);
                    double[] mat = tex.getTextureMatrix().getArray();
                    if(mat != null) {
                    float[] tmat = new float[16];
                    for (int i = 0; i < 4; i++) 
                        for (int j = 0;j<4;j++){
                       tmat[i + 4*j] = (float) mat[j+4*i];
                   }
                    map.put("matrix textureMatrix",tmat);
                    }
                    //map.put("sScale",new Float(tex.getTextureTransformation().getStretch()[0]));
                    //map.put("tScale",new Float(tex.getTextureTransformation().getStretch()[1]));
                    Ri.surface("transformedpaintedplastic",map);
                } else {
                    Ri.surface("plastic",map);
                }
            }
        } else {
            Ri.surface(slShader.getName(),slShader.getParameters());
        }
    }
    /**
     * @param tex
     * @return
     */
    private String writeTexture(Texture2D tex) {
            Image img;
            ImageData data = tex.getImage();
        String fname = (String) textures.get(data);
        if(fname == null) {
            fname = name+"_texture"+(textureCount++)+".tiff";
            File f = new File(fname);
            //Image img = tex.getImage().getImage();
            // TODO temporary as long as ImageData does not return a propper BufferedImage
            byte[] byteArray = data.getByteArray();
            int dataHeight = data.getHeight();
            int dataWidth = data.getWidth();
            BufferedImage bi = new BufferedImage(dataWidth, dataHeight,
                    BufferedImage.TYPE_INT_ARGB);
            WritableRaster raster = bi.getRaster();
            int[] pix = new int[4];
           
            for (int y = 0, ptr = 0; y < dataHeight; y++)
                for (int x = 0; x < dataWidth; x++, ptr += 4) {
                    pix[3] = byteArray[ptr + 3];
                    pix[0] = byteArray[ptr];
                    pix[1] = byteArray[ptr + 1];
                    pix[2] = byteArray[ptr + 2];
//                	pix[3] = byteArray[ptr + 0];
//                    pix[0] = byteArray[ptr + 3];
//                    pix[1] = byteArray[ptr + 2];
//                    pix[2] = byteArray[ptr + 1];
                    raster.setPixel(x, y, pix);
                }
            img = bi;
            //END of temp code...
            
            RenderedImage rImage =null;
            if( img instanceof RenderedImage )
                rImage = (RenderedImage) img;
            else {
                BufferedImage bImage =new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
                Graphics g =bImage.getGraphics();
                g.drawImage(img,0,0,null);
                rImage =bImage;
            }
            //System.out.println( Arrays.asList(ImageIO.getWriterFormatNames()));
//            RenderedImage image = tex.getImage();
//            String format = "tiff";
//
//            RenderedOp op = JAI.create("filestore", image,
//                    fname, format);
            try {
                //OutputStream os = new FileOutputStream(f);
                boolean worked =ImageIO.write(rImage,"tiff",f);
                if(!worked) System.err.println("writing of "+fname+" did not work!");
                //os.close();
                textures.put(data,fname);
            } catch (IOException e) {
                e.printStackTrace();
                fname = null;
            }
        }
        return fname;
    }
    boolean testBallStick = true;
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.IndexedLineSet)
     */
    public void visit(IndexedLineSet g) {
        String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
        if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW),true)) {
        
        DataList dl = g.getEdgeAttributes(Attribute.INDICES);
        if(dl!=null){
//            String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
//            if(!eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW),true)) return;
            Ri.attributeBegin();
            setupShader(eAppearance,CommonAttributes.LINE_SHADER);
        
            float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
//        int n= g.getNumEdges();
//        for(int i = 0;i<n;i++) {
//            cylinder(g.getEdgeData(i),r);
//        }
//        	
          // A test to get tubes drawn correctly for non-euclidean case (also fixes some problems I've noticed with
          // the euclidean case too.  -gunn 20.04.06
          // can't use the Renderman Cylinder primitive since it doesn't correspond to equidistant surfaces in non-euclidean cases.
          if (testBallStick)		{
        	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
             Color cc = (Color) eAppearance.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
             BallAndStickFactory bsf = new BallAndStickFactory(g);
        	  	bsf.setSignature(sig);
        	  	bsf.setStickRadius(r);
        	  	bsf.setShowBalls(false);	// need to actually omit the balls
        	  	bsf.setStickColor(cc);
        	  	bsf.update();
        	  	visit(bsf.getSceneGraphComponent());
          } else {
              IntArrayArray edgeIndices= dl.toIntArrayArray();
              DoubleArrayArray edgeColors=null;
              dl = g.getEdgeAttributes(Attribute.COLORS);
              if(dl != null) 
                  edgeColors = dl.toDoubleArrayArray();
              DoubleArrayArray vertices=g.getVertexAttributes(Attribute.COORDINATES)
              .toDoubleArrayArray();
              for (int i= 0, n=edgeIndices.size(); i < n; i++)
              {
                  if(edgeColors!= null) {
                      float[] f = new float[3];
                      f[0] = (float) edgeColors.getValueAt(i,0);
                      f[1] = (float) edgeColors.getValueAt(i,1);
                      f[2] = (float) edgeColors.getValueAt(i,2);
                      Ri.color(f);
                  }
                  IntArray edge=edgeIndices.item(i).toIntArray();
                  for(int j = 0; j<edge.getLength()-1;j++) {
                      DoubleArray p1=vertices.item(edge.getValueAt(j)).toDoubleArray();
                      DoubleArray p2=vertices.item(edge.getValueAt(j+1)).toDoubleArray();
                  //pipeline.processLine(p1, p2);
                  //pipeline.processPseudoTube(p1, p2);
                      cylinder(p1,p2,r);
                  }
              }

          }
          Ri.attributeEnd();
       	  

        }
        }
        super.visit(g);
    }

    /**
     * @param ds
     * @param r
     */
    private void cylinder(DoubleArray p1, DoubleArray p2, float r) {
        double d[] =new double[3];
//        d[0] =ds[3] - ds[0];
//        d[1] =ds[4] - ds[1];
//        d[2] =ds[5] - ds[2];
        d[0] = p2.getValueAt(0) - p1.getValueAt(0);
        d[1] = p2.getValueAt(1) - p1.getValueAt(1);
        d[2] = p2.getValueAt(2) - p1.getValueAt(2);
        float l =(float) Rn.euclideanNorm(d);
        d[0]/= l;
        d[1]/= l;
        d[2]/= l;
		double[] mat = MatrixBuilder.euclidean().translate(p1.getValueAt(0),p1.getValueAt(1),p1.getValueAt(2)).getMatrix().getArray();
        
        dirToEuler(d);
//        t.rotateZ(disk[5]);
//        t.rotateY(disk[4]);
//        t.rotateX(disk[3]);
//        t.rotateX(Math.PI/2.);
        //VecMat.assignRotationX(rot,Math.PI/2.);
        //VecMat.multiplyFromRight(mat,rot);
        
//        VecMat.assignRotationZ(rot,d[2]);
//        VecMat.multiplyFromRight(mat,rot);
//        VecMat.assignRotationY(rot,d[1]);
//        VecMat.multiplyFromRight(mat,rot);
//        VecMat.assignRotationX(rot,d[0] - Math.PI/2.);
//        VecMat.multiplyFromRight(mat,rot);

		double[] rot = MatrixBuilder.euclidean().rotateZ(d[2]).rotateY(d[1]).rotateX(d[0] - Math.PI/2.).getMatrix().getArray();
		
        Ri.transformBegin();
        Ri.concatTransform(fTranspose(mat));
        Ri.cylinder(r,0,l,360,null);
        Ri.transformEnd();
    }
        
    
    
    private IndexedFaceSet [] splitIfsToPrimitiveFaces(IndexedFaceSet ifs){
    	// Author Bernd Gonska
    	int num=ifs.getNumFaces();
    	IndexedFaceSet [] parts= new IndexedFaceSet[num];
    	for (int i=0;i<num;i++){
    		parts[i]= new IndexedFaceSet();
    		parts[i].setNumFaces(1);
    		parts[i].setNumPoints(ifs.getNumPoints());
    		parts[i].setVertexAttributes(ifs.getVertexAttributes());
    		
    		int[][]    oldIndizeesArray=null;
    		String[]   oldLabelsArray=null;
    		double[][] oldNormalsArray=null;
    		double[][] oldTextureCoordsArray=null;
    		int[][]    newIndizeesArray= new int[1][];
    		String[]   newLabelsArray=new String[1];
    		double[][] newNormalsArray=new double[1][];
    		double[][] newTextureCoordsArray=new double[1][];
    		
    		DataList temp=ifs.getFaceAttributes( Attribute.INDICES );
    		if (temp !=null){
    			oldIndizeesArray	= temp.toIntArrayArray(null);
    			newIndizeesArray[0] = oldIndizeesArray[i]; 
    			parts[i].setFaceAttributes(Attribute.INDICES,
    					new IntArrayArray.Array(newIndizeesArray));
    		}
    		
    		temp= ifs.getVertexAttributes( Attribute.LABELS );
    		if (temp!=null){
    			oldLabelsArray 		= temp.toStringArray(null);
    			newLabelsArray[0]	= oldLabelsArray[i];
    			parts[i].setFaceAttributes(Attribute.LABELS,
    					new StringArray(newLabelsArray));
    		}
    		temp= ifs.getVertexAttributes( Attribute.NORMALS );
    		if (temp!=null){
    			oldNormalsArray 	= temp.toDoubleArrayArray(null);
    			newNormalsArray[0] = oldNormalsArray[i]; 
    			parts[i].setFaceAttributes(Attribute.NORMALS,
    					new DoubleArrayArray.Array(newNormalsArray));
    		}
    		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
    		if (temp!=null){
    			oldTextureCoordsArray = temp.toDoubleArrayArray(null);
    			newTextureCoordsArray[0] = oldTextureCoordsArray[i]; 
    			parts[i].setFaceAttributes(Attribute.TEXTURE_COORDINATES,
    					new DoubleArrayArray.Array(newTextureCoordsArray));
    		}
    	}
    	return parts;
    }
    
    public void visit(IndexedFaceSet i) {
    	visit(i,null);
    }
    
  
    public void visit(IndexedFaceSet i,float[] color) {
// dieser teil ist neu:    	
    	DataList colors=i.getFaceAttributes( Attribute.COLORS );
    	if (colors !=null){
    		double[][] colorArray = colors.toDoubleArrayArray(null);
    		int numFaces=i.getNumFaces();
    		float[][] colorArrayf= new float[numFaces][3];
    		for (int k=0;k<numFaces;k++){
    			for (int j=0;j<3;j++)
    				colorArrayf[k][j]=(float)colorArray[k][j];
    		}
    		IndexedFaceSet[] faceList=splitIfsToPrimitiveFaces(i);
    		for (int k=0;k<numFaces;k++){			
    			visit(faceList[k],colorArrayf[k]);
    		}
    	}
    	else{
 // Neues ist hier zu ende
    		String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
    		if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.FACE_DRAW),true)) {
                
    			int npolys =i.getNumFaces();
    			if(npolys!= 0) {
    				HashMap map = new HashMap();
    				//boolean smooth = !((String)eAppearance.getAttribute(CommonAttributes.POLYGON_SHADER,"default")).startsWith("flat");
    				boolean smooth = eAppearance.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
    				DataList coords = i.getVertexAttributes(Attribute.COORDINATES);
    				DoubleArrayArray da = coords.toDoubleArrayArray();
    				int pointlength = GeometryUtility.getVectorLength(coords);
    				// We'd like to be able to use the "Pw" attribute which accepts 4-vectors for point coordinates, but 3Delight-5.0.1
    				// does not support it ...  
    				// TODO figure out how to allow Pw output if desired, for example, if you have Pixar renderman renderer.
//        if (pointlength == 3)	{
    				float[] fcoords =new float[3*da.getLength()];
    				for (int j = 0; j < da.getLength(); j++) {
    					if (pointlength == 4)	{
    						float w =(float)da.getValueAt(j,3);
    						if (w != 0) w = 1.0f/w;
    						else w = 10E10f;		// hack! but what else can you do?
    						fcoords[3*j+0] =(float)da.getValueAt(j,0)*w;
    						fcoords[3*j+1] =(float)da.getValueAt(j,1)*w;
    						fcoords[3*j+2] =(float)da.getValueAt(j,2)*w;
    					} else {
    						fcoords[3*j+0] =(float)da.getValueAt(j,0);
    						fcoords[3*j+1] =(float)da.getValueAt(j,1);
    						fcoords[3*j+2] =(float)da.getValueAt(j,2);
    					}
    				}
    				map.put("P",fcoords);       	
//        } else if (pointlength == 4)	{
//            float[] fcoords =new float[4*da.getLength()];
//            for (int j = 0; j < da.getLength(); j++) {
//                fcoords[3*j+0] =(float)da.getValueAt(j,0);
//                fcoords[3*j+1] =(float)da.getValueAt(j,1);
//                fcoords[3*j+2] =(float)da.getValueAt(j,2);
//                fcoords[3*j+3] =(float)da.getValueAt(j,3);
//            }
//            map.put("Pw",fcoords);       	
//        }
    				DataList normals = i.getVertexAttributes(Attribute.NORMALS);
    				if(smooth && normals!=null) {
    					da = normals.toDoubleArrayArray();
    					float[] fnormals =new float[3*da.getLength()];
    					for (int j = 0; j < da.getLength(); j++) {
    						fnormals[3*j+0] =(float)da.getValueAt(j,0);
    						fnormals[3*j+1] =(float)da.getValueAt(j,1);
    						fnormals[3*j+2] =(float)da.getValueAt(j,2);
    					}
    					map.put("N",fnormals);
    				} else { //face normals
    					normals = i.getFaceAttributes(Attribute.NORMALS);
    					if (normals != null)	{
    						da = normals.toDoubleArrayArray();
    						float[] fnormals =new float[3*da.getLength()];
    						for (int j = 0; j < da.getLength(); j++) {
    							fnormals[3*j+0] =(float)da.getValueAt(j,0);
    							fnormals[3*j+1] =(float)da.getValueAt(j,1);
    							fnormals[3*j+2] =(float)da.getValueAt(j,2);
    						}
    						map.put("uniform normal N",fnormals);            	
    					}
    				}
    				// texture coords:
    				DataList texCoords = i.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
    				if(texCoords!= null) {
    					float[] ftex =new float[2*texCoords.size()];
    					for (int j = 0; j < texCoords.size(); j++) {
    						//ftex[j] =(float)d.getValueAt(j);
    						DoubleArray l =texCoords.item(j).toDoubleArray();
                
    						ftex[2*j] =(float)l.getValueAt(0);
    						ftex[2*j+1] =(float)l.getValueAt(1);
    						//ftex[2*j] =(float)d.getValueAt(j,0);
    						//ftex[2*j+1] =(float)d.getValueAt(j,1);
    					}
    					map.put("st",ftex);
    				}
        
    				// texture coords:
    				DataList vertexColors = i.getVertexAttributes(Attribute.COLORS);
    				if(vertexColors!= null) {
    					int vertexColorLength=vertexColors.getStorageModel().getDimensions()[1];
    					float[] vCol =new float[3*vertexColors.size()];
    					float[] vOp=null;
    					if(vertexColorLength == 4 ) vOp = new float[3*vertexColors.size()];
    					for (int j = 0; j < vertexColors.size(); j++) {
    						//ftex[j] =(float)d.getValueAt(j);
    						DoubleArray rgba = vertexColors.item(j).toDoubleArray();
                
    						vCol[3*j]   =(float)rgba.getValueAt(0);
    						vCol[3*j+1] =(float)rgba.getValueAt(1);
    						vCol[3*j+2] =(float)rgba.getValueAt(2);
    						if(vertexColorLength ==4) {
    							vOp[3*j]    =(float)rgba.getValueAt(3);
    							vOp[3*j+1]  =(float)rgba.getValueAt(3);
    							vOp[3*j+2]  =(float)rgba.getValueAt(3);
    						}
    						//ftex[2*j] =(float)d.getValueAt(j,0);
    						//ftex[2*j+1] =(float)d.getValueAt(j,1);
    					}
    					map.put("varying color Cs",vCol);
    					if(vertexColorLength == 4 ) map.put("varying color Os",vOp);
    				}
        
        
    				int[] nvertices =new int[npolys];
    				int verticesLength =0;
    				for(int k =0; k<npolys;k++) {
    					IntArray fi = i.getFaceAttributes(Attribute.INDICES).item(k).toIntArray();
    					nvertices[k] =fi.getLength();
    					verticesLength+= nvertices[k];
    				}
    				int[] vertices =new int[verticesLength];
    				int l =0;
    				for(int k= 0;k<npolys;k++) {
    					for(int m =0; m<nvertices[k];m++,l++) {
    						IntArray fi = i.getFaceAttributes(Attribute.INDICES).item(k).toIntArray();
    						vertices[l] = fi.getValueAt(m);
    					}
    				}
    				Ri.attributeBegin();
    				setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
    				if(color!=null){
    					float[] f=new float[3];
						f[0]=color[0];f[1]=color[1];f[2]=color[2];
						Ri.color(f);
    					if (color.length==4){
    						f[0]=color[3];f[1]=color[3];f[2]=color[3];
    						Ri.opacity(f);
    						}	
    				}
    				Ri.pointsPolygons(npolys,nvertices,vertices,map);
    				Ri.attributeEnd();
    			}
    		}
    		super.visit(i);
    	}// neu
    }
    
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.PointSet)
     */
    public void visit(PointSet p) {
        String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
        if(!eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.VERTEX_DRAW),CommonAttributes.VERTEX_DRAW_DEFAULT)) return;
        int n= p.getNumPoints();
        DataList coord=p.getVertexAttributes(Attribute.COORDINATES);
        if(coord == null) return;
        Ri.attributeBegin();
        float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
        //System.out.println("point radius is "+r);
        setupShader(eAppearance,CommonAttributes.POINT_SHADER);
        boolean drawSpheres = eAppearance.getAttribute(CommonAttributes.SPHERES_DRAW,CommonAttributes.SPHERES_DRAW_DEFAULT);
        if(drawSpheres) {
        	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
             Color cc = (Color) eAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
             //DoubleArrayArray a=coord.toDoubleArrayArray();
             double[][] a=coord.toDoubleArrayArray(null);
             Ri.color(cc.getRGBColorComponents(null));
           double[] trns = new double[16];
            for (int i= 0; i < n; i++) { 
                trns = MatrixBuilder.init(null, sig).translate(a[i]).getArray();
                Ri.transformBegin();
                Ri.concatTransform(fTranspose(trns));
                HashMap map =new HashMap();
                Ri.sphere(r,-r,r,360f,map);
                Ri.transformEnd();
                //pipeline.processPoint(a, i);            
            }
        } else {
            HashMap map =new HashMap();
            double[] pc = new double[3*n];
            coord.toDoubleArray(pc);
            float[] pcf = new float[3*n];
            for (int i = 0; i < pcf.length; i++) {
                pcf[i] = (float) pc[i];
            }
            map.put("P",pcf);
            map.put("constant float constantwidth",new Float(r));
            Ri.points(n,map);
        }
            Ri.attributeEnd();
    }
    
    public void visit(ClippingPlane p) {
        Ri.clippingPlane(0,0,0,0,0,1);
    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.UnitSphere)
     */
    public void visit(Sphere s) {
        //setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        Ri.sphere(1f,-1f,1f,360f,null);
    }
    
    public void visit(Cylinder c) {
        //setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        Ri.cylinder(1f,-1f,1f,360f,null);
        Ri.disk(-1f,1f,360f,null);
        Ri.disk(1f,1f,360f,null);
        
    }

    /**
     * @return Returns the height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * @param proj The style of Projection.
     */
    public void projection(String proj){
    	this.proj=proj;
    }
    
    /**
     * @param maximumEyeSplits.
     */
    public void setMaximumEyeSplits(int maximumEyeSplits){
    	this.maximumEyeSplits[0]=maximumEyeSplits;
    }
    
    
    private static void dirToEuler(double r[]) {
        double d =Rn.euclideanNorm(r);
        double x = r[0]/d;
        double y = r[1]/d;
        double z = r[2]/d;
        
        double xrot = 0;
        double zrot = 0;
        double yrot = 0;
        
//  if(x*x+y*y -0.0001> 0.) {
//      xrot =  -Math.acos(z);
//      zrot =  Math.atan2(y,x);
//  }
        if(z*z +x*x -0.000000001> 0.) {
            xrot =  Math.acos(y);
            yrot =  Math.atan2(x,z);
        } else {
            xrot =  (y>0?0:Math.PI);
            yrot = 0;
        }    
        
        r[0] = xrot;
        r[1] = yrot;
        r[2] = zrot;
    }

}
