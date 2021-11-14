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

/* This is a non-euclidean polygon shader.
 * It does not have all the features of the standard DefaultPolygonShader.
 * Author:  Charles Gunn
 */
vec4 Ambient;
vec4 Diffuse;
vec4 Specular;
vec4 texcoord;
attribute vec4 normals4;
uniform bool hyperbolic;
uniform bool elliptic;
uniform float metric;
uniform bool 	useNormals4;
uniform bool 	lightingEnabled; 
uniform bool 	fogEnabled;
uniform bool 	transparencyEnabled;
uniform bool 	twoSided;
uniform float Nw;
uniform float	transparency;
uniform sampler2D texture;
uniform int numLights;
uniform int numTextures;
uniform bool poincareModel;
uniform bool azimuthProjection;
uniform mat4 cam2world, world2cam;
uniform mat4 H2NDC;

// the inner product in klein model of hyperbolic space
float dot4(in vec4 P, in vec4 Q)	{
	return dot(P.xyz, Q.xyz) + metric* P.w*Q.w;
}

// the derived length function
float length4(in vec4 P)	{
    return sqrt(abs(dot4(P,P)));
}

float acosh(in float x) {
    return log(abs(x) + sqrt(abs(x*x-1.0)));
}

float asinh(in float x) {
    return log(abs(x) + sqrt(abs(x*x+1.0)));
}

float nedistance(in vec4 a, in vec4 b)    {
    float d = dot4(a,b)/sqrt(abs(dot4(a,a)*dot4(b,b)));
    return hyperbolic ? acosh(d) : acos(d);
}

// project the vector T into the hyperbolic tangent space of P
void projectToTangent(in vec4 P, inout vec4 T) {
		T = (dot4(P,P) * T - dot4(P,T) * P);
}

// find the representative of the given point with length +/- 1
void normalize4(inout vec4 P)	{
    P = (1.0/length4(P))*P;
    if (hyperbolic && P.w < 0.0) P = -P;
}
 
// adjust T to be a unit tangent vector to the point P
void normalize4(in vec4 P, inout vec4 T)	{
	projectToTangent(P,T);
	normalize4(T);
}

void dehomogenize(inout vec4 P4)	 {
    P4 = (P4.w == 0.0 ? 1.0 : (1.0/P4.w)) * P4;
}

// calculate the lighting incident on a position with given normal vector and 
// given eye position (lights are available as global array).
void pointLight(in int i, in vec4 normal, in vec4 eye, in vec4 eyePosition4)
{
   float nDotVP;       // normal . light direction
   float nDotHV;       // normal . light half vector
   float pf;           // power factor
   float attenuation;  // computed attenuation factor
   float d;            // nedistance from surface to light source
   vec4  toLight;           // direction from surface to light position
   vec4  halfVector;   // direction of maximum highlights

   // Compute nedistance between surface and light position
   toLight = gl_LightSource[i].position;
    d = nedistance(toLight, eyePosition4);
    // Normalize the vector from surface to light position
   normalize4(eyePosition4, toLight );
//    if (!hyperbolic && toLight.w * eyePosition4.w < 0.0)
//        toLight = -toLight;

 //   Compute attenuation
   if (hyperbolic) 
       attenuation = gl_LightSource[i].constantAttenuation + exp(-gl_LightSource[i].linearAttenuation * d);
   else
       attenuation =  gl_LightSource[i].constantAttenuation + (2.0 - gl_LightSource[i].linearAttenuation)*abs(cos(d));

    //    attenuation = clamp(attenuation, 0.0, 1.0);
    halfVector = (toLight + eye);
    if (hyperbolic) 
		halfVector = -halfVector;
   normalize4(eyePosition4, halfVector); 
    nDotVP = abs(dot4(normal, toLight)); //max(0.0, dot4(normal, toLight));
   nDotHV = max(0.0, dot4(normal, halfVector));

   if (nDotVP == 0.0) pf = 0.0;
   else 
		pf = pow(nDotHV, gl_FrontMaterial.shininess);

   Ambient  += gl_LightSource[i].ambient * attenuation;
    Diffuse  += gl_LightSource[i].diffuse * nDotVP * attenuation;
   Specular += gl_LightSource[i].specular * pf * attenuation;
}

//void ftexgen(in vec4 normal, in vec4 eyePosition)
//{
//    gl_TexCoord[0] = gl_TextureMatrix[0]*gl_MultiTexCoord0;
//}

vec4 light(in vec4 normal, in vec4 eyePosition, in gl_MaterialParameters matpar)
{
    vec4 color;
    vec4 eye = vec4(0.0, 0.0, 0.0, 1.0);
    int i;
    float fog = 0.0, d2eye=0.0, alpha;
    if (fogEnabled)	{
      	d2eye = nedistance(eye, eyePosition); 
 //       if (!hyperbolic && eyePosition.w < 0.0) d2eye = d2eye + 1.0;
    	fog = exp(-d2eye * gl_Fog.density);
    	fog = clamp(fog, 0.0, 1.0);
    }   
    // Clear the light intensity accumulators
    if (lightingEnabled)	{
      normalize4(eyePosition, eye);
      Ambient  = Diffuse = Specular = vec4 (0.0);
        for ( i = 0; i<numLights; ++i)	{
    	    pointLight(i, normal, eye, eyePosition);
        }
   		color = gl_FrontLightModelProduct.sceneColor +
      	    Ambient  * matpar.ambient +
      	    Diffuse  * gl_Color + //*matpar.diffuse  +
      	    Specular * matpar.specular;
    } else  {
   		color = gl_Color + gl_FrontLightModelProduct.sceneColor +
      	   matpar.ambient;
    }

    color = clamp( color, 0.0, 1.0 );
    if (fogEnabled) color = mix( (gl_Fog.color), color, fog);
    color.a = 1.0-transparency;
    if (color.a != 0.0 && !transparencyEnabled) color.a = 1.0;
   return color;
}

void main (void)
{
    vec4 origin = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 n4 = (useNormals4) ? gl_MultiTexCoord3 : vec4(gl_Normal, 0.0);
    vec4  transformedNormal = gl_ModelViewMatrix * n4;
    normalize4(transformedNormal);
    vec4 eyePosition = gl_ModelViewMatrix * gl_Vertex ;
    normalize4(eyePosition);
    if (hyperbolic && eyePosition.w < 0.0) eyePosition = -eyePosition;
    normalize4(eyePosition, transformedNormal);
    if (hyperbolic && transformedNormal.w * transformedNormal.z > 0.0 ) 
    	transformedNormal = -transformedNormal;
// set the texture coordinate
    gl_TexCoord[0] = texcoord = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;
    gl_FrontColor = light(transformedNormal, eyePosition, gl_FrontMaterial);
    transformedNormal = -transformedNormal;
    gl_BackColor = light(transformedNormal, eyePosition, gl_BackMaterial);
//    if (exp(-nedistance(eyePosition, origin)*gl_Fog.density) > .9) gl_FrontColor = vec4(0.0, 1.0, 0.0, 1.0);
//    if (gl_BackColor.r + gl_BackColor.g + gl_BackColor.b < .01) gl_BackColor = gl_FrontColor;
//    else if (gl_FrontColor.r + gl_FrontColor.g + gl_FrontColor.b < .01) gl_FrontColor = gl_BackColor;
    if (hyperbolic && poincareModel)	{
      	vec4 p4 =  cam2world * eyePosition;
     	dehomogenize(p4);
        p4 = 1.0/(1.0 + sqrt(1.0-dot(p4.xy,p4.xy))) * p4;
 	    p4.w = 1.0;
     	gl_Position = gl_ProjectionMatrix * (world2cam * p4); 
    } else if ( azimuthProjection)	{
       	vec4 p4 =  cam2world * eyePosition;
        float d = nedistance(origin, p4);
        if (elliptic && p4.w < 0.0) p4.w = -p4.w;
	    dehomogenize(p4);
        float r = dot(p4.xy,p4.xy);
        if (r > .001) p4 = (d/sqrt(r)) * p4;
 	    p4.w = 1.0;
     	gl_Position = gl_ProjectionMatrix * (world2cam*p4); 
    }
    else     
        gl_Position = ftransform();
//    gl_BackColor = .075 * gl_BackColor;
//    gl_BackColor.a = 2 * gl_BackColor.a;
}

