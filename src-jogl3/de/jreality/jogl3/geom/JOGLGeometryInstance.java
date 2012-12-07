package de.jreality.jogl3.geom;

import java.awt.Color;
import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.GLShader.ShaderVar;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

public abstract class JOGLGeometryInstance extends SceneTreeNode {

	public class GlTexture{
		boolean hasTexture = false;
		public GlTexture(){
			
		}
		private Texture2D tex = null;
		public void setTexture(Texture2D tex){
			this.tex = tex;
			hasTexture = true;
		}
		public void removeTexture(){
			hasTexture = false;
		}
		public void bind(GLShader shader, GL3 gl){
			if(hasTexture){
				//GL_TEXTURE0 reserved for lights.
				Texture2DLoader.load(gl, tex, gl.GL_TEXTURE1);
				gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "image"), 1);
				gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "hasTex"), 1);
			}else{
				gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "hasTex"), 0);
			}
		}
	}
	
	public abstract class GlUniform<T>{
		public GlUniform(String name, T value){
			this.name = name;
			this.value = value;
		}
		public String name;
		public T value;
		
		public abstract void bindToShader(GLShader shader, GL3 gl);
		
		
	}
	public class GlUniformSampler extends GlUniform<Integer>{
		public Texture2D tex;
		public GlUniformSampler(String name, Integer value, Texture2D tex) {
			super(name, value);
			this.tex = tex;
		}

		@Override
		public void bindToShader(GLShader shader, GL3 gl) {
			Texture2DLoader.load(gl, tex, gl.GL_TEXTURE1);
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, name), value);
		}
	}
	
	public class GlUniformInt extends GlUniform<Integer>{

		public GlUniformInt(String name, Integer value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, name), value);
		}
	}
	public class GlUniformFloat extends GlUniform<Float>{

		public GlUniformFloat(String name, Float value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			gl.glUniform1f(gl.glGetUniformLocation(shader.shaderprogram, name), value);
		}
	}
	public class GlUniformVec4 extends GlUniform<float[]>{

		public GlUniformVec4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			gl.glUniform4fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, value, 0);
		}
	}
	public class GlUniformMat4 extends GlUniform<float[]>{

		public GlUniformMat4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, true, value, 0);
        }
	}
	public class GlUniformVec3 extends GlUniform<float[]>{

		public GlUniformVec3(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			gl.glUniform3fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, value, 0);
		}
	}
//	public class UniformCollection{
//		public LinkedList<GlUniform<Integer>> intUniforms = new LinkedList<GlUniform<Integer>>();
//		public LinkedList<GlUniform<Float>> floatUniforms = new LinkedList<GlUniform<Float>>();
//		public LinkedList<GlUniform<float[]>> vec3Uniforms = new LinkedList<GlUniform<float[]>>();
//		public LinkedList<GlUniform<float[]>> vec4Uniforms = new LinkedList<GlUniform<float[]>>();
//		public LinkedList<GlUniform<Integer>> sampler2DUniforms = new LinkedList<GlUniform<Integer>>();
//	}
	
	//TODO make private
	public EffectiveAppearance eap;
	
	//TODO
	//public GLShader;
	//public Appearance Attributes for this shader;
	
	protected JOGLGeometryInstance(Geometry node) {
		super(node);
	}

	public abstract void render(JOGLRenderState state);

	
	//this method copies appearance attributes to a list of uniform variables for later use in the openGL shader
	//it furthermore returns the openGL shader to use
	protected GLShader updateAppearance(SceneGraphPath sgp, GL3 gl, LinkedList<GlUniform> c, GlTexture texture, String type) {
		GLShader shader = GLShader.defaultPolygonShader;
		if(type.equals("lineShader"))
			shader = GLShader.defaultLineShader;
		if(type.equals("pointShader"))
			shader = GLShader.defaultPointShader;
		
		//		System.out.println("UpdateAppearance");
		eap = EffectiveAppearance.create(sgp);
//		if(type.equals(CommonAttributes.POLYGON_SHADER)){
//			System.out.println("start eap for " + sgp.getLastComponent().getName());
//			//System.out.println(((IndexedFaceSet)(fse.getNode())).getName());
//			
//			//eap.getApp().getAttributes().keySet()
//			for( Object o : eap.getApp().getAttributes().keySet()){
//				String s = (String)o;
//				eap.getApp().getAttribute(s);
//				Object a = new Object();
//				System.out.println(s + " " + eap.getAttribute(s, a).getClass());
//			}
//			System.out.println("stop");
//		}
		//retrieve shader source if existent
		String[] source = new String[]{};
		
		source = (String[])eap.getAttribute(type + "::glsl330-source", source);
		// has attribute key like "polygonShader::glsl330-source"
		// and an array of two Strings
		if(source != null && source.length == 2){
			//TODO problem here! we are not passing back the pointer...
			shader = new GLShader(source[0], source[1]);
			shader.init(gl);
		}
		//TODO retrieve and save shader attributes in a sensible
		//fashion
		boolean hasTexture = false;
		for(ShaderVar v : shader.shaderUniforms){
			//if(type.equals(CommonAttributes.POINT_SHADER))
				//System.out.println("shader var is " + v.getName() + ", type is " + v.getType());
    		if(v.getName().equals("numGlobalDirLights"))
    			continue;
    		if(v.getName().equals("numGlobalPointLights"))
    			continue;
    		if(v.getName().equals("numGlobalSpotLights"))
    			continue;
    		if(v.getName().equals("globalLights"))
    			continue;
    		if(v.getName().equals("projection"))
    			continue;
    		if(v.getName().equals("modelview")){
    			continue;
    		}
    		if(v.getName().equals("screenSize")){
    			continue;
    		}
    		if(v.getName().equals("screenSizeInSceneOverScreenSize")){
    			continue;
    		}
    		if(v.getName().equals("hasTex")){
    			continue;
    		}
    		if(v.getName().length() > 3 && v.getName().substring(0, 4).equals("has_")){
    			continue;
    		}
    		//System.out.println("updateAppearance " + v.getName());
    		//TODO exclude some more like light samplers, camPosition
    		//retrieve corresponding attribute from eap
    		if(v.getType().equals("int")){
    			Object value = new Object();
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()), value);
    			if(value.getClass().equals(Integer.class)){
    				c.add(new GlUniformInt(v.getName(), (Integer)value));
    				//c.intUniforms.add(new GlUniform<Integer>(v.getName(), (Integer)value));
    				//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), (Integer)value);
    			}else if(value.getClass().equals(Boolean.class)){
    				boolean b = (Boolean)value;
    				int valueInt = 0;
        			if(b){
        				valueInt = 1;
        			}
        			c.add(new GlUniformInt(v.getName(), valueInt));
        			//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), valueInt);
    			}else{
    				c.add(new GlUniformInt(v.getName(), 0));
    			}
    		}
    		else if(v.getType().equals("vec4")){
//    			System.out.println(v.getName());
    			Object value = new Object();
    			//System.out.println(v.getName());
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()), value);
    			
    			if(value.getClass().equals(Color.class)){
    				float[] color = ((Color)value).getRGBComponents(null);
    				//System.out.println(sgp.getLastComponent().getName() + type + "." + v.getName() + color[0] + " " + color[1] + " " + color[2]);
    				c.add(new GlUniformVec4(v.getName(), color));
    			}else if(value.getClass().equals(float[].class)){
    				c.add(new GlUniformVec4(v.getName(), (float[])value));
    			}else if(value.getClass().equals(double[].class)){
    				double[] value2 = (double[])value;
    				c.add(new GlUniformVec4(v.getName(), Rn.convertDoubleToFloatArray(value2)));
    			}else{
    				//default value
    				c.add(new GlUniformVec4(v.getName(), new float[]{0, 0, 0, 1}));
    			}
    		}
    		else if(v.getType().equals("float")){
//    			System.out.println(v.getName());
    			Object value = new Object();
    			//System.out.println(v.getName());
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()), value);
    			
    			if(value.getClass().equals(Double.class)){
    				Double value2 = (Double)value;
    				c.add(new GlUniformFloat(v.getName(), value2.floatValue()));
    			}else if(value.getClass().equals(Float.class)){
    				c.add(new GlUniformFloat(v.getName(), (Float)value));
    			}else{
    				c.add(new GlUniformFloat(v.getName(), 0f));
    			}
    		}else if(v.getType().equals("sampler2D") && v.getName().equals("image")){
    			//ImageData value = new Object();
    			//value = eap.getAttribute(ShaderUtility.nameSpace(type, "texture2d:image"), value);
    			//MyEntityInterface mif = (MyEntityInterface) AttributeEntityFactory.createAttributeEntity(MyEntityInterface.class, &quot;myEntityName&quot;, ea);
    			//Texture2D tex = (Texture2D)
    			if(AttributeEntityUtility.hasAttributeEntity(Texture2D.class, type + ".texture2d", eap)){
    				Texture2D tex = (Texture2D)AttributeEntityUtility.createAttributeEntity(Texture2D.class, type + ".texture2d", eap);
    				texture.setTexture(tex);
    				c.add(new GlUniformMat4("textureMatrix", Rn.convertDoubleToFloatArray(tex.getTextureMatrix().getArray())));
    				System.err.println("sampler2D");
    				hasTexture = true;
    			}
    		}else{
    			System.err.println(v.getType() + " not implemented this type yet. have to do so in JOGLGeometryInstance.updateAppearance(...).");
    		}
    		//TODO other possible types, textures
    	}
		if(!hasTexture){
			texture.removeTexture();
		}
		return shader;
	}

	public abstract void updateAppearance(SceneGraphPath sgp, GL3 gl);
}