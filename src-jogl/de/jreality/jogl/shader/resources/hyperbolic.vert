/*******************************************************
*  Fixed.vert Fixed Function Equivalent Vertex Shader  *
*   Automatically Generated by 3Dlabs GLSL ShaderGen   *
*             http://developer.3dlabs.com              *
*******************************************************/
vec4 Ambient;
vec4 Diffuse;
vec4 Specular;

// the inner product in klein model of hyperbolic space
float dot31(in vec4 P, in vec4 Q)	{
	return P.x*Q.x+P.y*Q.y+P.z*Q.z-P.w*Q.w;
}

// the derived length function
float length31(in vec4 P)	{
    return sqrt(abs(dot31(P,P)));
}

float acosh(in float x) {
    return log(abs(x) + sqrt(x*x-1.0));
}

float distance31(in vec4 a, in vec4 b)    {
    float aa = dot31(a,a);
    float ab = dot31(a,b);
    float bb = dot31(b,b);
    float d = ab/sqrt(aa*bb);
    return d;
}
// project the vector T into the hyperbolic tangent space of P
vec4 projectToTS31(in vec4 P, in vec4 T) {
	return T - (dot31(P,T)/dot31(P,P)) * P;
}

// set a vector to a specified hyperbolic length.
vec4 setToLength31(in vec4 P, in float d)	{
    float l = length31(P);
    float f = d/l;
    return f * P;
}

// find the representative of the given point with length 1
void normalize31(inout vec4 P)	{
    float l = 1.0/length31(P);
    P = l * P;
 }
 
// adjust T to be a unit tangent vector to the point P
vec4 normalize31(in vec4 P, in vec4 T)	{
	vec4 X = projectToTS31(P, T);
	normalize31(X);
	return X;
}

// calculate the lighting incident on a position with given normal vector and 
// given eye position (lights are available as global array).
void pointLight(in int i, in vec4 normal, in vec4 eye, in vec4 ecPosition4)
{
   float nDotVP;       // normal . light direction
   float nDotHV;       // normal . light half vector
   float pf;           // power factor
   float attenuation;  // computed attenuation factor
   float d;            // distance from surface to light source
   vec4  VP;           // direction from surface to light position
   vec4  halfVector;   // direction of maximum highlights

   // Compute vector from surface to light position
   VP = gl_LightSource[i].position - ecPosition4;

   // Compute distance between surface and light position
   //d = length31(VP);
   d = distance31(gl_LightSource[i].position, ecPosition4);

   // Normalize the vector from surface to light position
   VP = normalize31(ecPosition4, VP);

 //   Compute attenuation
//   attenuation = 1.0 / (gl_LightSource[i].constantAttenuation +
//       gl_LightSource[i].linearAttenuation * d +
//       gl_LightSource[i].quadraticAttenuation * d * d);
   attenuation = 1.0;

    halfVector = normalize31(ecPosition4, VP + eye);

   nDotVP = max(0.0, dot31(normal, VP));
   nDotHV = max(0.0, dot31(normal, halfVector));

   if (nDotVP == 0.0)
   {
       pf = 0.0;
   }
   else
   {
       pf = pow(nDotHV, gl_FrontMaterial.shininess);

   }
   Ambient  += gl_LightSource[i].ambient * attenuation;
   Diffuse  += gl_LightSource[i].diffuse * nDotVP * attenuation;
   Specular += gl_LightSource[i].specular * pf * attenuation;
}

void ftexgen(in vec4 normal, in vec4 ecPosition)
{

    gl_TexCoord[0] = gl_TextureMatrix[0]*gl_MultiTexCoord0;
}

void light(vec4 normal, vec4 ecPosition) {
   vec4 eye = vec4(0.0, 0.0, 1.0, 0.0);
    // Clear the light intensity accumulators
    Ambient  = vec4 (0.0);
    Diffuse  = vec4 (0.0);
    Specular = vec4 (0.0);

    pointLight(0, normal, eye, ecPosition);

    pointLight(1, normal, eye, ecPosition);
}

void flight(in vec4 normal, in vec4 ecPosition)
{
    vec4 color;
    light(normal, ecPosition);
    color = gl_FrontLightModelProduct.sceneColor +
      Ambient  * gl_FrontMaterial.ambient +
      Diffuse  * gl_FrontMaterial.diffuse;
    color += Specular * gl_FrontMaterial.specular;
    color = clamp( color, 0.0, 1.0 );
    gl_FrontColor = color;
}

void blight(in vec4 normal, in vec4 ecPosition)
{
    vec4 color;
    light(normal, ecPosition);
    color = gl_BackLightModelProduct.sceneColor +
      Ambient  * gl_BackMaterial.ambient +
      Diffuse  * gl_BackMaterial.diffuse;
    color += Specular * gl_BackMaterial.specular;
    color = clamp( color, 0.0, 1.0 );
    gl_BackColor = color;
}

void main (void)
{
    vec4  transformedNormal = gl_ModelViewMatrix * vec4( gl_Normal, 1.0);
    vec4 ecPosition = gl_ModelViewMatrix * gl_Vertex ;
    normalize31(ecPosition);
    transformedNormal = normalize31(ecPosition, transformedNormal);
    // Do fixed functionality vertex transform
    // attempt to debug by moving points in the normal direction
    //gl_Position = gl_ModelViewProjectionMatrix * (gl_Vertex - vec4(.5) * transformedNormal); //ftransform();
    flight(transformedNormal, ecPosition);
//    transformedNormal = transformedNormal;
//    blight(transformedNormal, ecPosition);
//    if (dot31(ecPosition, ecPosition) > 0.0) gl_FrontColor = vec4(1,0,0,1);
    float inpro = dot31(ecPosition, transformedNormal);
    //if (dot31(ecPosition, ecPosition) > 0.0) 
        gl_FrontColor = vec4(inpro,1.0-inpro,0.0, 1.0);
     ftexgen(transformedNormal, ecPosition);
}

