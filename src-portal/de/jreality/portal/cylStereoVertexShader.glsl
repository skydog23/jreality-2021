/*******************************************************
*  Fixed.vert Fixed Function Equivalent Vertex Shader  *
*   Automatically Generated by 3Dlabs GLSL ShaderGen   *
*             http://developer.3dlabs.com              *
*******************************************************/
vec4 Ambient;
vec4 Diffuse;
vec4 Specular;

uniform vec4 cv; // centerViewport: minx, maxx, miny, maxy
uniform float d; // distance to the screen plane
uniform float near;
uniform float far;
uniform float eye;
uniform float eyeSep;
uniform float screenRotation;

const float PI2 = 1.5707963267948966;

void pointLight(in int i, in vec3 normal, in vec3 eye, in vec3 ecPosition3)
{
   float nDotVP;       // normal . light direction
   float nDotHV;       // normal . light half vector
   float pf;           // power factor
   float attenuation;  // computed attenuation factor
   float d;            // distance from surface to light source
   vec3  VP;           // direction from surface to light position
   vec3  halfVector;   // direction of maximum highlights

   // Compute vector from surface to light position
   VP = vec3 (gl_LightSource[i].position) - ecPosition3;

   // Compute distance between surface and light position
   d = length(VP);

   // Normalize the vector from surface to light position
   VP = normalize(VP);

   // Compute attenuation
   attenuation = 1.0 / (gl_LightSource[i].constantAttenuation +
       gl_LightSource[i].linearAttenuation * d +
       gl_LightSource[i].quadraticAttenuation * d * d);

   halfVector = normalize(VP + eye);

   nDotVP = max(0.0, dot(normal, VP));
   nDotHV = max(0.0, dot(normal, halfVector));

   if (nDotVP == 0.0)
   {
       pf = 0.0;
   }
   else
   {
       // oops this is a dependency on the front material! Fix!
       pf = pow(nDotHV, gl_FrontMaterial.shininess);

   }
   Ambient  += gl_LightSource[i].ambient * attenuation;
   Diffuse  += gl_LightSource[i].diffuse * nDotVP * attenuation;
   Specular += gl_LightSource[i].specular * pf * attenuation;
}

void directionalLight(in int i, in vec3 normal, in vec3 eye, in vec3 ecPosition3)
{
   float nDotVP;         // normal . light direction
   float nDotHV;         // normal . light half vector
   float pf;             // power factor

   vec3  VP;           // direction from surface to light position
   vec3  halfVector;   // direction of maximum highlights
   
   // Compute vector from surface to light position
   VP = normalize(gl_LightSource[i].position.xyz);
   halfVector = normalize(VP + eye);
   
   nDotVP = max(0.0, dot(normal, VP));
   nDotHV = max(0.0, dot(normal, halfVector));
//   nDotHV = max(0.0, dot(normal, gl_LightSource[i].halfVector.xyz));

   if (nDotVP == 0.0)
   {
       pf = 0.0;
   }
   else
   {
       pf = pow(nDotHV, gl_FrontMaterial.shininess);

   }
   Ambient  += gl_LightSource[i].ambient;
   Diffuse  += gl_LightSource[i].diffuse * nDotVP;
   Specular += gl_LightSource[i].specular * pf;
}

float ffog(in float ecDistance)
{
    return(abs(ecDistance));
}

vec3 fnormal(void)
{
    //Compute the normal 
    vec3 normal = gl_NormalMatrix * gl_Normal;
    normal = normalize(normal);
    return normal;
}

void ftexgen(in vec3 normal, in vec4 ecPosition)
{

    gl_TexCoord[0] = gl_TextureMatrix[0]*gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1]*gl_MultiTexCoord1;
}

void flight(in vec3 normal, in vec3 ecPosition3, in vec3 eye, float alphaFade, float theta)
{
    vec4 color;

    // Clear the light intensity accumulators
    Ambient  = vec4 (0.0);
    Diffuse  = vec4 (0.0);
    Specular = vec4 (0.0);

    directionalLight(0, normal, eye, ecPosition3);

    directionalLight(1, normal, eye, ecPosition3);

    directionalLight(2, normal, eye, ecPosition3);

    directionalLight(3, normal, eye, ecPosition3);

	vec4 dc = vec4(0., 0., 0., 1.);
	if (theta>0.) dc.x= theta/2./PI2;
	else dc.y=-theta/2./PI2;

    color = gl_FrontLightModelProduct.sceneColor +
      Ambient  * gl_FrontMaterial.ambient +
      Diffuse  * gl_FrontMaterial.diffuse;
    color += Specular * gl_FrontMaterial.specular;

	//color = dc;
    
    color = clamp( color, 0.0, 1.0 );
    gl_FrontColor = color;

    //gl_FrontColor.a *= alphaFade;
}

void blight(in vec3 normal, in vec3 ecPosition3, in vec3 eye, float alphaFade, float theta)
{
    vec4 color;
 
    // Clear the light intensity accumulators
    Ambient  = vec4 (0.0);
    Diffuse  = vec4 (0.0);
    Specular = vec4 (0.0);

    directionalLight(0, normal, eye, ecPosition3);

    directionalLight(1, normal, eye, ecPosition3);

    directionalLight(2, normal, eye, ecPosition3);

    directionalLight(3, normal, eye, ecPosition3);

	vec4 dc = vec4(0., 0., 0., 1.);
	if (theta>0.) dc.x= theta/2./PI2;
	else dc.y=-theta/2./PI2;

    color = gl_BackLightModelProduct.sceneColor +
      Ambient  * gl_BackMaterial.ambient +
      Diffuse  * gl_BackMaterial.diffuse;
    color += Specular * gl_BackMaterial.specular;

	//color = dc;
	
    color = clamp( color, 0.0, 1.0 );
    gl_BackColor = color;

    //gl_BackColor.a *= alphaFade;
}

mat3 rotMat3(in vec3 axis, in float angle) {
	vec3 u = normalize(axis);
	
	float c = cos(angle);
	float s = sin(angle);
	float v = 1.0 - c;
	
	mat3 m = mat3(
	 u[0] * u[0] * v + c,
	 u[0] * u[1] * v + u[2] * s,
	 u[0] * u[2] * v - u[1] * s, // columns

	 u[1] * u[0] * v - u[2] * s,
	 u[1] * u[1] * v + c,
	 u[1] * u[2] * v + u[0] * s,

	 u[2] * u[0] * v + u[1] * s,
	 u[2] * u[1] * v - u[0] * s,
	 u[2] * u[2] * v + c);
	return m;
}

mat4 makePerspectiveProjectionMatrix(in vec4 viewport, in vec4 c)	{
	float an = abs(near);
	float l = viewport[0] * an;
	float r = viewport[1] * an;
	float b = viewport[2] * an;
	float t = viewport[3] * an;
	
	mat4 m = mat4(
	2.*near/(r-l),
	0.,
	0.,
	0.,
	
	0.,
	2.*near/(t-b),
	0.,
	0.,
	
	(r+l)/(r-l),
	(t+b)/(t-b),
	(far+near)/(near-far),
	-1.0,
	
	0.,
	0.,
	2.*near*far/(near-far),
	0.);
	
	mat4 cc = mat4(
	1.,	0.,	0.,	0.,
	
	0.,	1.,	0.,	0.,
	
	0.,	0.,	1.,	0.,
	
	-c.x, -c.y,	-c.y, 1.);
	
	return m*cc;
}
	
vec4 transformViewport(in vec4 c) {
	float fscale = 1./(d+c.z);
	vec4 ret = vec4(fscale*(cv.x*d-c.x), 
					 fscale*(cv.y*d-c.x), 
					 fscale*(cv.z*d-c.y), 
					 fscale*(cv.w*d-c.y));
	if (ret.x>ret.y) {
		float t = ret.y;
		ret.y=ret.x;
		ret.x=t;
	}
	if (ret.z>ret.w) {
		float t = ret.w;
		ret.w=ret.z;
		ret.z=t;
	}
	return ret;
}
	
void main (void)
{

    bool fast=false;

    // Eye-coordinate position of vertex, needed in various calculations
    vec4 p = gl_ModelViewMatrix * gl_Vertex;
    
    p /= p.w;

	float a = eyeSep/2.; // half eye separation

	// the point in eye coords (projected in the y=0-plane)
	vec3 pp = p.xyz;
	pp.y=0.0;
    float r=length(pp.xyz);

// camera tangent to eye circle:
    float alpha = acos(a/r);
    float beta = atan(p.x, -p.z);
    float thetaL = beta+alpha;
    float thetaR = beta-alpha;
  
// camera on antipodal points of eye circle:
	thetaR = -PI2+beta;
	thetaL = PI2+beta;

    float theta = (eye == 0.) ? thetaL : thetaR;
    
    vec3 axis = vec3(0.0, 1.0, 0.0);
    
    // real camera pos in old coords
    vec4 c = vec4(-a*sin(theta), 0., a*cos(theta), 0.);
    
    vec4 ecPosition = p-c;
    
    vec3  transformedNormal;
    transformedNormal = fnormal();
    
    float alphaFade = 1.0;
    

    // local lighting for new cam position        
//    vec3 ecPosition3 = (vec3 (ecPosition)) / ecPosition.w;
//    vec3 eye = -normalize(ecPosition3);
// lighting from untransformed camera and non-local light model
    vec3 ecPosition3 = p.xyz;
	vec3 eye = vec3(0., 0., 1.);
        
    flight(transformedNormal, ecPosition3, eye, alphaFade, theta);
    transformedNormal = -transformedNormal;
    blight(transformedNormal, ecPosition3, eye, alphaFade, theta);
    gl_FogFragCoord = ffog(ecPosition.z);
    ftexgen(transformedNormal, ecPosition);

	if (!fast) {
		vec4 vpn=transformViewport(c);
		mat4 proj = makePerspectiveProjectionMatrix(vpn, c);
		gl_Position = proj*ecPosition;
	} else {
		vec4 P = vec4(p.x-c.x+(p.x*c.z-c.x*p.z)/d, p.y-c.y+(p.y*c.z-c.y-p.z)/d, p.z-c.z, 1.);
		gl_Position = gl_ProjectionMatrix*P;
	}
}

