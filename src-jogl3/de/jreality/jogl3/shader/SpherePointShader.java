package de.jreality.jogl3.shader;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlReflectionMap;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLPointSetEntity;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.math.Rn;

public class SpherePointShader{
	
	public static void render(JOGLPointSetEntity pse, LinkedList<GlUniform> c, GlReflectionMap reflMap, GLShader shader, JOGLRenderState state){
		//System.out.println("LineShader.render()");
		
		GL3 gl = state.getGL();
		
		state.getLightHelper().loadLocalLightTexture(state.getLocalLightCollection(), gl);
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] modelview = Rn.convertDoubleToFloatArray(state.getModelViewMatrix());
		float[] inverseCamMatrix = Rn.convertDoubleToFloatArray(state.inverseCamMatrix);
		shader.useShader(gl);
		
    	//matrices
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "projection"), 1, true, projection, 0);
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "modelview"), 1, true, modelview, 0);
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "_inverseCamRotation"), 1, true, inverseCamMatrix, 0);
    	
    	//global lights in a texture
    	gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_globalLights"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalDirLights"), state.getLightHelper().getNumGlobalDirLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalPointLights"), state.getLightHelper().getNumGlobalPointLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalSpotLights"), state.getLightHelper().getNumGlobalSpotLights());
		
		//local lights in a texture
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_localLights"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalDirLights"), state.getLightHelper().getNumLocalDirLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalPointLights"), state.getLightHelper().getNumLocalPointLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalSpotLights"), state.getLightHelper().getNumLocalSpotLights());
		
    	
		//bind shader uniforms
		for(GlUniform u : c){
			//System.out.println("Uniform in TubesLineShader: " + u.name);
			u.bindToShader(shader, gl);
		}
		//TODO all the other types
		reflMap.bind(shader, gl);
		
		//TODO TODO TODO
		GLVBO sphereVBO = state.getSphereHelper().getSphereVBO(gl, 4);
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, sphereVBO.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, "sphere_coords"), sphereVBO.getElementSize(), sphereVBO.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "sphere_coords"));
		
    	//bind vbos to corresponding shader variables
    	List<ShaderVar> l = shader.vertexAttributes;
    	for(ShaderVar v : l){
    		GLVBO vbo = pse.getPointVBO(v.getName());
    		if(vbo != null){
    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 1);
    			gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo.getID());
            	
    			gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), vbo.getElementSize(), vbo.getType(), false, 4*vbo.getElementSize(), 0);
            	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
            	//important here: we advance to the next element only after all of tube_coords have been drawn.
            	gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), 1);
            	
    		}else{
    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 0);
    		}
    	}
    	
    	state.getLightHelper().bindGlobalLightTexture(gl);
      	//actual draw command
    	//gl.glDrawArrays(gl.GL_LINES, 0, lse.getLineVBO("vertex_coordinates").getLength()/2);
    	//gl.glDrawArrays(mode, first, count);
    	gl.glDrawArraysInstanced(gl.GL_TRIANGLES, 0, sphereVBO.getLength()/4, pse.getPointVBO("vertex_coordinates").getLength()/4);
    	//gl.glDrawElementsInstancedBaseVertex(mode, count, type, indices, primcount, basevertex);
    	
    	//disable all vbos
    	for(ShaderVar v : l){
    		GLVBO vbo = pse.getPointVBO(v.getName());
    		if(vbo != null){
    			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
    			gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), 0);
    		}
    	}
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "sphere_coords"));
    	
		shader.dontUseShader(gl);
	}
}
