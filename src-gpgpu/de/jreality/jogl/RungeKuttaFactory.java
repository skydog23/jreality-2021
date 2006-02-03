package de.jreality.jogl;

import java.util.HashSet;
import java.util.Iterator;

/**
 * factory for easy creation of glsl code for runge kutta integration on the gpu
 * @author weissman
 *
 */
public class RungeKuttaFactory {

  private static final String RK2 =
    "void main(void) {" +
    "  vec2 pos = gl_TexCoord[0].st;\n" + 
    "  vec4 pt = textureRect(values, pos);\n" + 
    "  vec4 k1 = h * evaluateT0(pt);\n" + 
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" + 
    "  vec4 res = pt + k2;\n" + 
    "  if (r3) res.w = 1.;\n" + 
    "  gl_FragColor = res;\n" +
    "}\n";

  private static final String RK4 =
    "void main(void) {" +
    "  vec2 pos = gl_TexCoord[0].st;\n" +
    "  vec4 pt = textureRect(values, pos);\n" +
    "  vec4 k1 = h * evaluateT0(pt);\n" +
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" +
    "  vec4 k3 = h * evaluateT0_H2(pt+k2/2.);\n" +
    "  vec4 k4 = h * evaluateT0_H(pt+k3);\n" +
    "  vec4 res = pt + (k1 + 2.*(k2 + k3) + k4)/6.;\n" +
    "  if (r3) res.w = 1.;\n" + 
    "  gl_FragColor = res;\n" +
    "}\n";
  
  private int order;
  private HashSet uniforms=new HashSet();
  private HashSet signatures=new HashSet();
  private HashSet methods=new HashSet();

  private RungeKuttaFactory(int order) {
    this.order=order;
    addUniform("h", "float");
    addUniform("r3", "bool");
    addUniform("values", "samplerRect");
  }
  

  /**
   * create a factory for runge kutta integration of order 2
   * @return the factory
   */
  public static RungeKuttaFactory rk2() {
    return new RungeKuttaFactory(2);
  }
  
  /**
   * create a factory for runge kutta integration of order 2
   * @return the factory
   */
  public static RungeKuttaFactory rk4() {
    return new RungeKuttaFactory(4);
  }

  /**
   * add a uniform parameter to the src code
   * @param name the name of the uniform param
   * @param type the type of the param
   */
  public void addUniform(String name, String type) {
    uniforms.add("uniform "+type+" "+name+";");
  }
  
  public void addMethod(String name, String retType, String params, String implementation) {
    String signature = retType+" "+name+"("+params+")";
    String impl = signature+"{\n"+implementation+"}\n";
    signatures.add(signature);
    methods.add(impl);
  }
  
  public void srcT0(String impl) {
    addMethod("evaluateT0", "vec4", "const vec4 point", impl);
  }

  public void srcT0_H2(String impl) {
    addMethod("evaluateT0_H2", "vec4", "const vec4 point", impl);
  }

  public void srcT0_H(String impl) {
    if (order == 2) throw new IllegalStateException("no such method for order 2"); 
    addMethod("evaluateT0_H", "vec4", "const vec4 point", impl);
  }

  public void srcAll(String impl) {
    srcT0(impl);
    srcT0_H2(impl);
    if (order == 4) srcT0_H(impl);
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (Iterator i = uniforms.iterator(); i.hasNext(); )
      sb.append(i.next()).append('\n');
    sb.append('\n');
    for (Iterator i = signatures.iterator(); i.hasNext(); )
      sb.append(i.next()).append(';').append('\n');
    sb.append('\n');
    sb.append(order == 2 ? RK2 : RK4).append('\n');
    for (Iterator i = methods.iterator(); i.hasNext(); )
      sb.append(i.next()).append('\n');
    sb.append('\n');
    return sb.toString();
  }
}
