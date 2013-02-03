package de.jreality.jogl3.shader;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.GLShader.ShaderVar;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLLineSetEntity;
import de.jreality.math.Rn;

public class TubesLineShader{
	
	public static void render(JOGLLineSetEntity lse, LinkedList<GlUniform> c, GLShader shader, JOGLRenderState state){
		//System.out.println("LineShader.render()");
		
		GL3 gl = state.getGL();
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] modelview = Rn.convertDoubleToFloatArray(state.getModelViewMatrix());
		shader.useShader(gl);
		
    	//matrices
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "projection"), 1, true, projection, 0);
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "modelview"), 1, true, modelview, 0);
    	
		//directional lights
    	
		//bind shader uniforms
		for(GlUniform u : c){
			u.bindToShader(shader, gl);
		}
		//TODO all the other types
		
		GLVBO tubeVBO = state.getTubeHelper().getLineVBO(gl, 4);
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tubeVBO.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, "tube_coords"), tubeVBO.getElementSize(), tubeVBO.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "tube_coords"));
		
    	//bind vbos to corresponding shader variables
    	List<ShaderVar> l = shader.vertexAttributes;
    	for(ShaderVar v : l){
    		GLVBO vbo = lse.getLineVBO(v.getName());
    		if(vbo != null){
    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 1);
    			gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo.getID());
            	
    			gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), vbo.getElementSize(), vbo.getType(), false, 2*4*vbo.getElementSize(), 0);
            	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
            	//important here: we advance to the next element only after all of tube_coords have been drawn.
            	gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), 1);
            	
            	gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, "_"+v.getName()), vbo.getElementSize(), vbo.getType(), false, 2*4*vbo.getElementSize(), 4*vbo.getElementSize());
            	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "_"+v.getName()));
            	//important here: we advance to the next element only after all of tube_coords have been drawn.
            	gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, "_"+v.getName()), 1);
//            	
            	
    		}else{
    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 0);
    		}
    	}
    	
    	//actual draw command
    	//gl.glDrawArrays(gl.GL_LINES, 0, lse.getLineVBO("vertex_coordinates").getLength()/2);
    	//gl.glDrawArrays(mode, first, count);
    	gl.glDrawArraysInstanced(gl.GL_TRIANGLES, 0, tubeVBO.getLength()/4, lse.getLineVBO("vertex_coordinates").getLength()/8);
    	//gl.glDrawElementsInstancedBaseVertex(mode, count, type, indices, primcount, basevertex);
    	
    	//disable all vbos
    	for(ShaderVar v : l){
    		GLVBO vbo = lse.getLineVBO(v.getName());
    		if(vbo != null){
    			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
    			gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), 0);
//    			
    			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "_"+v.getName()));
    			gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, "_"+v.getName()), 0);
    		}
    	}
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "tube_coords"));
    	
		shader.dontUseShader(gl);
	}
}
