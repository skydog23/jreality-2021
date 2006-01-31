package de.jreality.jogl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import de.jreality.jogl.shader.GlslLoader;
import de.jreality.scene.Appearance;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.GlslSource;

public abstract class AbstractGpgpuViewer extends Viewer {

  private boolean doIntegrate;
  
  private boolean tex2D;
  private int TEX_TARGET;
  private int TEX_INTERNAL_FORMAT;
  private static int TEX_FORMAT = GL.GL_RGBA;
  private boolean atiHack;
  
  private GlslProgram program;
  
  private int[] fbos = new int[1]; // 1 framebuffer
  private int[] valueTextures = new int[2]; // ping pong textures
  private int[] attachments = {GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT};
  private int readTex, writeTex = 1;

  private FloatBuffer valueBuffer;
  private int valueTextureSize;
  private int numValues;

  private boolean valuesChanged;
  private boolean valueTextureSizeChanged;
  private boolean hasValues;

  private boolean readData=true;

  public AbstractGpgpuViewer(boolean tex2D) {
    super();
    this.tex2D = tex2D;
    atiHack=tex2D;
    TEX_TARGET = tex2D ? GL.GL_TEXTURE_2D : GL.GL_TEXTURE_RECTANGLE_NV;
    TEX_INTERNAL_FORMAT = tex2D ? GL.GL_RGBA32F_ARB : GL.GL_FLOAT_RGBA32_NV;
  }
  
  public void display(GLDrawable drawable) {
    //GL gl = new DebugGL(drawable.getGL());
    GL gl = drawable.getGL();
    GLU glu = drawable.getGLU();
    gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    if (doIntegrate && hasValues) {
      
      gl.glEnable(TEX_TARGET);
      
      initPrograms(gl);
      initFBO(gl);
      initViewport(gl, glu);
      initTextures(gl);

      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
          attachments[readTex], TEX_TARGET, valueTextures[readTex], 0);      
      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
          attachments[writeTex], TEX_TARGET, valueTextures[writeTex], 0);

      GpgpuUtility.checkBuf(gl);
      
      gl.glDrawBuffer(attachments[writeTex]);
      
      // set all values
      prepareUniformValues(gl, program);
      
      GlslLoader.render(program, drawable);
      
      renderQuad(gl);
      
      gl.glFinish();

      GpgpuUtility.checkBuf(gl);

      if (readData) {
        valueBuffer.clear();
        transferFromTexture(gl, valueBuffer);
        if (atiHack) GpgpuUtility.atiHack(valueBuffer);
      } else {
//          transferFromTextureToVBO(gl);
      }
      
      // do swap
      int tmp = readTex;
      readTex = writeTex;
      writeTex = tmp;
  
      // switch back to old buffer
      gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

      GlslLoader.postRender(program, drawable); // any postRender just resets the shader pipeline
      doIntegrate=false;
      gl.glDisable(TEX_TARGET);
      calculationFinished();
    }
    
    gl.glPopAttrib();
    gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    super.display(drawable);
    gl.glPopAttrib();
    		
  }

  protected void calculationFinished() {
  }

  protected void prepareUniformValues(GL gl, GlslProgram prog) {
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(TEX_TARGET, valueTextures[readTex]);
    prog.setUniform("values", 0);
  }

  protected GlslSource updateSource() {
    return null;
  }

  protected abstract GlslSource initSource();
  
  private void renderQuad(GL gl) {
    gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
    gl.glBegin(GL.GL_QUADS);
      gl.glTexCoord2d(0.0, 0.0);
      gl.glVertex2d(0.0, 0.0);
      gl.glTexCoord2d(tex2D ? 1 : valueTextureSize, 0.0);
      gl.glVertex2d(valueTextureSize, 0.0);
      gl.glTexCoord2d(tex2D ? 1 : valueTextureSize, tex2D ? 1 : valueTextureSize);
      gl.glVertex2d(valueTextureSize, valueTextureSize);
      gl.glTexCoord2d(0.0, tex2D ? 1 : valueTextureSize);
      gl.glVertex2d(0.0, valueTextureSize);
    gl.glEnd();
  }

  private void initPrograms(GL gl) {
    GlslSource src = program == null ? initSource() : updateSource();
    if (src == null) return;
    if (program != null) GlslLoader.dispose(gl, program);
    program = new GlslProgram(new Appearance(), "foo", src);
  }

  private void initTextures(GL gl) {
    if (valueTextureSizeChanged) {
      gl.glEnable(TEX_TARGET);
      if (valueTextures[0] != 0) {
        gl.glDeleteTextures(2, valueTextures);
      }
      gl.glGenTextures(2, valueTextures);
      setupTexture(gl, valueTextures[0], valueTextureSize);
      setupTexture(gl, valueTextures[1], valueTextureSize);
      valueTextureSizeChanged=false;
      System.out.println("[initTextures] new particles tex size: "+valueTextureSize);
    }
    if (valuesChanged) {
      gl.glEnable(TEX_TARGET);
      valueBuffer.clear();
      transferToTexture(gl, valueBuffer, valueTextures[readTex], valueTextureSize);
      System.out.println("[initTextures] new particle data");
//      if (!readData) {
//          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbos[0]);
//          gl.glBufferDataARB(GL.GL_PIXEL_PACK_BUFFER_EXT, theWidth*theWidth*4*4, particleBuffer, GL.GL_STREAM_COPY);
//          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
//          hasValidVBO=true;
//      }
      valuesChanged=false;
    }
  }

  private void setupTexture(GL gl, int i, int size) {
    gl.glBindTexture(TEX_TARGET, i);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
    gl.glTexImage2D(TEX_TARGET, 0, TEX_INTERNAL_FORMAT, size, size, 0,
        TEX_FORMAT, GL.GL_FLOAT, (float[]) null);
  }

  /**
   * Transfers data from currently texture, and stores it in given array.
   */
  void transferFromTexture(GL gl, FloatBuffer data) {
    // version (a): texture is attached
    // recommended on both NVIDIA and ATI
    gl.glReadBuffer(attachments[writeTex]);
    gl.glReadPixels(0, 0, valueTextureSize, valueTextureSize, TEX_FORMAT, GL.GL_FLOAT, data);

    // version b: texture is not neccessarily attached
//    gl.glBindTexture(TEX_TARGET, particleTexs[writeTex]);
//    gl.glGetTexImage(TEX_TARGET, 0, TEX_FORMAT, GL.GL_FLOAT, data.clear());

  }

  /**
   * Transfers data to texture.
   */
  void transferToTexture(GL gl, FloatBuffer buffer, int texID, int size) {
    // version (a): HW-accelerated on NVIDIA
    gl.glBindTexture(TEX_TARGET, texID);
    gl.glTexSubImage2D(TEX_TARGET, 0, 0, 0, size, size, TEX_FORMAT,
        GL.GL_FLOAT, buffer);

    // version (b): HW-accelerated on ATI
    //    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, textureParameters.texTarget, texID, 0);
    //    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
    //    glRasterPos2i(0,0);
    //    glDrawPixels(texSize,texSize,textureParameters.texFormat,GL_FLOAT,data);
  }

  private void initFBO(GL gl) {
    if (fbos[0] == 0) {
      gl.glGenFramebuffersEXT(1, fbos);
      System.out.println("created FBO=" + fbos[0]);
    }
    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbos[0]);
  }

//  private void initVBO(GL gl) {
//    if (vbos[0] == 0) {
//      gl.glGenBuffersARB(1, vbos);
//      System.out.println("created VBO=" + vbos[0]);
//    }
//  }
  
  private void initViewport(GL gl, GLU glu) {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0, valueTextureSize, 0, valueTextureSize);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glViewport(0, 0, valueTextureSize, valueTextureSize);
  }
  
  public FloatBuffer getCurrentValues() {
    if (!readData) return null;
    valueBuffer.position(0).limit(numValues);
    return valueBuffer.asReadOnlyBuffer();
  }

  public void setValues(float[] values) {
    System.out.println("GpgpuViewer.setParticles()");
    if (numValues != values.length) {
      int texSize = GpgpuUtility.texSize(values.length/4);
      if (valueTextureSize!=texSize) {
        System.out.println("[setParticles] new particles tex size="+texSize);
        valueBuffer = ByteBuffer.allocateDirect(texSize*texSize*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        valueTextureSize=texSize;
        valueTextureSizeChanged=true;
      }
      numValues = valueTextureSize*valueTextureSize*4;
    }
    
    valueBuffer.position(0).limit();
    valueBuffer.put(values);
    
    for(
      valueBuffer.position(numValues).limit(valueBuffer.capacity());
      valueBuffer.hasRemaining();
      valueBuffer.put(0f)
    );
    
    valuesChanged=true;
    hasValues=true;
  }
      
  public boolean isReadData() {
  	return readData;
  }
  
  public void setReadData(boolean readData) {
  	this.readData = readData;
  }
  
  public int getValueTextureSize() {
    return valueTextureSize;
  }
  
  public void triggerCalculation() {
    doIntegrate=true;
  }
  
  protected boolean isTex2D() {
    return tex2D;
  }
}
