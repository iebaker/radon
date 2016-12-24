#version 400

in vec3 rn_VertexPosition;
in vec3 rn_VertexNormal;

uniform mat4 rn_EntityModel;
uniform mat4 rn_MeshModel;
uniform mat4 rn_View;
uniform mat4 rn_Projection;

out vec3 rn_Normal;

void main() {
    gl_Position = rn_Projection * rn_View * rn_EntityModel * rn_MeshModel * vec4(rn_VertexPosition, 1);
    rn_Normal = vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexNormal, 0));
}