#version 400

in vec3 rn_Normal;
in vec3 rn_Position;
in vec3 rn_DiffuseColor;
in vec3 rn_SpecularColor;
in float rn_SpecularExponent;

uniform vec3 rn_AmbientColor;
uniform float rn_AmbientCoefficient;

uniform int rn_DirectionalLightCount;
uniform int rn_PointLightCount;

uniform vec3 rn_DirectionalLightDirections[4];
uniform vec3 rn_DirectionalLightIntensities[4];
uniform vec3 rn_PointLightPositions[256];
uniform vec3 rn_PointLightIntensities[256];

uniform vec3 rn_CameraEye;

out vec4 rn_FragmentColor;

void main() {

    vec3 surfaceToCamera = normalize(rn_CameraEye - rn_Position);
    vec3 normal = normalize(rn_Normal);
    vec3 finalRgb = rn_AmbientCoefficient * rn_AmbientColor;

    for (int i = 0; i < rn_DirectionalLightCount; i++) {
        vec3 surfaceToLight = normalize(rn_DirectionalLightDirections[i]);
        vec3 incidence = -surfaceToLight;
        float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));
        finalRgb += diffuseCoefficient * rn_DiffuseColor * rn_DirectionalLightIntensities[i];

        vec3 reflection = reflect(incidence, normal);
        float cosAngle = max(0.0, dot(surfaceToCamera, reflection));
        float specularCoefficient = pow(cosAngle, rn_SpecularExponent);
        finalRgb += specularCoefficient * rn_SpecularColor * rn_DirectionalLightIntensities[i];
    }

    for (int i = 0; i < rn_PointLightCount; i++) {
        vec3 incidence = rn_Position - rn_PointLightPositions[i];
        float attenuation = 1 / dot(incidence, incidence);

        incidence = normalize(incidence);
        vec3 surfaceToLight = -incidence;
        float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));
        finalRgb += attenuation * diffuseCoefficient * rn_DiffuseColor * rn_PointLightIntensities[i];

        vec3 reflection = reflect(incidence, normal);
        float cosAngle = max(0.0, dot(surfaceToCamera, reflection));
        float specularCoefficient = pow(cosAngle, rn_SpecularExponent);
        finalRgb += attenuation * specularCoefficient * rn_SpecularColor * rn_PointLightIntensities[i];
    }

    rn_FragmentColor = vec4(finalRgb, 1.0f);
}