//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
//needed?
float shade = .5;

uniform vec4 diffuseColor;
uniform float diffuseCoefficient;

uniform vec4 ambientColor;
uniform float ambientCoefficient;

uniform vec4 specularColor;
uniform float specularCoefficient;
uniform float specularExponent;

uniform sampler2D image;
uniform int has_Tex;

in vec2 texCoord;
in vec4 camSpaceCoord;
in vec3 camSpaceNormal;

//GLOBAL LIGHTS
uniform sampler2D sys_globalLights;
uniform int sys_numGlobalDirLights;
uniform int sys_numGlobalPointLights;
uniform int sys_numGlobalSpotLights;

//LOCAL LIGHTS
uniform sampler2D sys_localLights;
uniform int sys_numLocalDirLights;
uniform int sys_numLocalPointLights;
uniform int sys_numLocalSpotLights;

uniform int has_vertex_texturecoordinates;

vec3 lightInflux = vec3(0, 0, 0);

float attenuation(vec3 att, float dist){
	return 1/(att.x+att.y*dist+att.z*dist*dist);
}

float spot(float exp, vec4 dir, vec4 relPos, float coneAngle){
	float angle = acos(dot(-normalize(relPos.xyz), dir.xyz));
	return pow(cos(angle), coneAngle);
}

void calculateLightInfluxGeneral(vec3 normal, int numDir, int numPoint, int numSpot, sampler2D lights){	
	
	//size of the light texture
	int lightTexSize = numDir*3+numPoint*3+numSpot*5;
	
	for(int i = 0; i < numDir; i++){
		vec4 dir = texture(lights, vec2((3*i+1+0.5)/lightTexSize, 0));
		
		vec4 col = texture(lights, vec2((3*i+0.5)/lightTexSize, 0));
		float intensity = texture(lights, vec2((3*i+2+0.5)/lightTexSize, 0)).r;
		vec4 ambient = ambientColor*col*intensity;
		lightInflux = lightInflux + ambientCoefficient * ambient.xyz;
		
		float dott = dot(normal, normalize(dir.xyz));
		if(dott > 0){
			vec4 diffuse = dott*diffuseColor*col*intensity;
			
			float spec = dot(camSpaceNormal, normalize(normalize(dir.xyz)-normalize(camSpaceCoord.xyz)));
			//this specularColor here seems to be diffuseColor*specularColor
			vec4 specular =specularColor*intensity*pow(spec, specularExponent);
			
			vec4 new = specularCoefficient*specular+diffuseCoefficient*diffuse;
			lightInflux = lightInflux + new.xyz;
		}
	}
	for(int i = 0; i < numPoint; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(lights, vec2((3*numDir+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(normal, normalize(RelPos.xyz));
		if(dott > 0){
			vec4 col = texture(lights, vec2((3*numDir+3*i+0.5)/lightTexSize, 0));
			vec4 tmp = texture(lights, vec2((3*numDir+3*i+2+0.5)/lightTexSize, 0));
			vec3 att = tmp.xyz;
			float intensity = tmp.a;
			float dist = length(RelPos);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = atten*dott*col*intensity;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	//this causes quite some overhead, when we have many spot lights, even if they are not visible for this pixel
	//the check, whether the light is on the right side of the triangle might better be done after checking whether it is inside the cone
	for(int i = 0; i < numSpot; i++){
		vec4 pos = texture(lights, vec2((3*numDir+3*numPoint+5*i+2+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(normal, normalize(RelPos.xyz));
		//light is on the right side of the face
		if(dott > 0){
			vec4 dir = texture(lights, vec2((3*numDir+3*numPoint+5*i+1+0.5)/lightTexSize, 0));
			vec4 coneAngles = texture(lights, vec2((3*numDir+3*numPoint+5*i+3+0.5)/lightTexSize, 0));
			float angle = acos(dot(-normalize(RelPos.xyz), dir.xyz));
			if(angle < coneAngles.x){
				vec4 col = texture(lights, vec2((3*numDir+3*numPoint+5*i+0.5)/lightTexSize, 0));
				float intensity = coneAngles.a;
				float factor = pow(cos(angle), coneAngles.z);
				vec3 att = texture(lights, vec2((3*numDir+3*numPoint+5*i+4+0.5)/lightTexSize, 0)).xyz;
				float dist = length(RelPos);
				float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
				vec4 new = atten*factor*dott*col*intensity;
				vec3 new2 = new.xyz;
				lightInflux = lightInflux + new2;
			}
		}
	}
}

void calculateGlobalLightInflux(vec3 normal){
	lightInflux = lightInflux + ambientColor.xyz*ambientCoefficient;
	calculateLightInfluxGeneral(normal, sys_numGlobalDirLights, sys_numGlobalPointLights, sys_numGlobalSpotLights, sys_globalLights);
}
void calculateLocalLightInflux(vec3 normal){
	calculateLightInfluxGeneral(normal, sys_numLocalDirLights, sys_numLocalPointLights, sys_numLocalSpotLights, sys_localLights);
}

void main(void)
{
	//calculateLightInflux();
	//TODO check for availability of texture, check for face colors, what is diffuseColor?
	//vec4 texCoord = textureMatrix * vec4(gl_PointCoord, 0, 1);
	vec4 texColor = texture( image, texCoord.st);
	
	vec4 color2 = vec4(1, 1, 1, 1);
	if(has_vertex_texturecoordinates==1 && has_Tex == 1)
		color2 = texColor;
	if(color2.a==0)
		discard;
	lightInflux = vec3(0, 0, 0);
	if(gl_FrontFacing){
		calculateGlobalLightInflux(camSpaceNormal);
		calculateLocalLightInflux(camSpaceNormal);
		gl_FragColor = vec4(color2.xyz*lightInflux, color2.a);
	}else{
		calculateGlobalLightInflux(-camSpaceNormal);
		calculateLocalLightInflux(-camSpaceNormal);
		gl_FragColor = vec4(color2.xyz*lightInflux, color2.a);
	}
}
