//author Benjamin Kutschan
//default line vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;

//the two endpoints of one edge
in vec4 vertex_coordinates;
in vec4 _vertex_coordinates;
//one of the many coordinates of the unit length/radius tube
in vec4 tube_coords;

in float vertex_relativeRadii;
in float _vertex_relativeRadii;
uniform int has_vertex_relativeRadii;

in float edge_relativeRadii;
//(not neccessary but not harmful either, just again edge_relativeRadii)
in float _edge_relativeRadii;
uniform int has_edge_relativeRadii;

in vec4 vertex_colors;
in vec4 _vertex_colors;
in vec4 edge_colors;
in vec4 _edge_colors;
out vec4 edgeColor;
out vec4 vertexColor;


uniform float tubeRadius;

uniform vec4 _jitter;

out vec4 camSpaceCoord;
out vec3 camSpaceNormal;

void main(void)
{
	float relRad = 1;
	if(has_vertex_relativeRadii == 1){
		if(tube_coords.x > 0.5)
			relRad = _vertex_relativeRadii;
		else
			relRad = vertex_relativeRadii;
	}
	if(has_edge_relativeRadii == 1)
		relRad = edge_relativeRadii;
	if(tube_coords.x > 0.5){
		vertexColor = _vertex_colors;
		edgeColor = _edge_colors;
	}else{
		vertexColor = vertex_colors;
		edgeColor = edge_colors;
	}
	//edge endpoints in camera space
	vec3 v1 = (modelview*vertex_coordinates).xyz;
	vec3 v2 = (modelview*_vertex_coordinates).xyz;
	vec3 newZ = normalize(v1-(v2-v1)*dot(v1, v2-v1)/dot(v2-v1, v2-v1));
	vec3 newY = normalize(cross(normalize(v2-v1), newZ));
	mat4 trafo = mat4(v2-v1, 0, newY, 0, newZ, 0, v1, 1);
	
	vec4 scaledTubeCoords = vec4(tube_coords.x, tube_coords.y*tubeRadius*relRad, tube_coords.z*tubeRadius*relRad, 1);
	
	gl_Position = projection * trafo * scaledTubeCoords;
	//gl_Position = gl_Position / gl_Position.w;
	gl_Position += _jitter;
	
	vec3 Normal = vec3(0, tube_coords.yz);
	mat3 rotation = mat3(vec3(trafo[0][0], trafo[0][1], trafo[0][2]), vec3(trafo[1][0], trafo[1][1], trafo[1][2]), vec3(trafo[2][0], trafo[2][1], trafo[2][2]));
	camSpaceNormal = normalize(rotation*Normal);
	camSpaceCoord = trafo*scaledTubeCoords;

	//gl_Position = projection * modelview * vec4(((vertex_coordinates.xyz +_vertex_coordinates.xyz)/2.0+tube_coords.xyz), 1);
	//gl_Position = vertex_coordinates;
}