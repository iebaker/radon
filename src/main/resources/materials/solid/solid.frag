#version 400

uniform vec3 rn_Color;

out vec4 rn_FragmentColor;

uniform vec3 rn_CameraEye;
uniform bool rn_UseDiscardPlane;
uniform vec4 rn_DiscardPlane;

void main() {
    if (rn_UseDiscardPlane) {
        float check = dot(rn_DiscardPlane, vec4(rn_CameraEye, 1.0)) * dot(rn_DiscardPlane, vec4(rn_Position, 1.0));
        if (check > 0) discard;
    }
    rn_FragmentColor = vec4(rn_Color, 1);
}