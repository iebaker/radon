#version 400

in vec3 rn_Normal;
in vec3 rn_Position;
in vec3 rn_DiffuseColor;
in vec3 rn_SpecularColor;
in float rn_SpecularExponent;

uniform vec3 rn_AmbientColor;
uniform float rn_AmbientCoefficient;
uniform float rn_DiffuseCoefficient;
uniform float rn_SpecularCoefficient;

uniform int rn_DirectionalLightCount;
uniform int rn_PointLightCount;

uniform vec3 rn_DirectionalLightDirections[4];
uniform vec3 rn_DirectionalLightIntensities[4];
uniform vec3 rn_PointLightPositions[256];
uniform vec3 rn_PointLighIntensities[256];

uniform vec3 rn_CameraEye;

out vec4 rn_FragmentColor;

float posDot(vec3 a, vec3 b) {
    return max(0, dot(a, b));
}

void main() {
    vec3 direction;
    vec3 halfAngle;
    vec3 diffuse;
    vec3 specular;
    vec3 look;
    vec3 finalRgb;

    float specularFactor;
    float diffuseFactor;
    float attenuation;

    finalRgb = rn_AmbientCoefficient * rn_AmbientColor;
    look = normalize(rn_CameraEye - rn_Position);

    for (int i = 0; i < rn_DirectionalLightCount; i++) {
        direction = normalize(rn_DirectionalLightDirections[i]);
        halfAngle = normalize(look + direction);
        diffuseFactor = rn_DiffuseCoefficient * posDot(rn_Normal, direction);
        specularFactor = rn_SpecularCoefficient * pow(posDot(rn_Normal, halfAngle), rn_SpecularExponent);
        diffuse = diffuseFactor * rn_DiffuseColor;
        specular = specularFactor * rn_SpecularColor;
        finalRgb += rn_DirectionalLightIntensities[i] * (diffuse + specular);
    }

    for (int i = 0; i < rn_PointLightCount; i++) {
        direction = normalize(rn_PointLightPositions[i] - rn_Position);
        attenuation = 1 / length(direction);
        halfAngle = normalize(look + direction);
        diffuseFactor = rn_DiffuseCoefficient * posDot(rn_Normal, direction);
        specularFactor = rn_SpecularCoefficient * pow(posDot(rn_Normal, halfAngle), rn_SpecularCoefficient);
        diffuse = diffuseFactor * rn_DiffuseColor;
        specular = specularFactor * rn_SpecularColor;
        finalRgb += attenuation * rn_PointLighIntensities[i] * (diffuse + specular);
    }

    rn_FragmentColor = vec4(finalRgb, 1.0f);
}