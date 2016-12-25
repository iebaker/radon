#version 400

in vec3 rn_VertexPosition;
in vec3 rn_VertexNormal;
in vec3 rn_VertexDiffuseColor;
in vec3 rn_VertexSpecularColor;
in vec3 rn_VertexEmissiveColor;
in float rn_VertexSpecularExponent;

uniform mat4 rn_MeshModel;
uniform mat4 rn_EntityModel;
uniform mat4 rn_View;
uniform mat4 rn_Projection;

out vec3 rn_Normal;
out vec3 rn_Position;
out vec3 rn_DiffuseColor;
out vec3 rn_SpecularColor;
out vec3 rn_EmissiveColor;
out float rn_SpecularExponent;

void main() {
    gl_Position = rn_Projection * rn_View * rn_EntityModel * rn_MeshModel * vec4(rn_VertexPosition, 1);

    rn_Position = vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexPosition, 1));
    rn_Normal = normalize(vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexNormal, 0)));
    rn_DiffuseColor = rn_VertexDiffuseColor;
    rn_SpecularColor = rn_VertexSpecularColor;
    rn_SpecularExponent = rn_VertexSpecularExponent;
    rn_EmissiveColor = rn_VertexEmissiveColor;
}