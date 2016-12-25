#version 400

in vec3 rn_Position;
in vec3 rn_Normal;

out vec4 rn_FragmentColor;

uniform vec3 rn_CameraEye;
uniform bool rn_UseDiscardPlane;
uniform vec4 rn_DiscardPlane;

void main() {
    if (rn_UseDiscardPlane) {
        float check = dot(rn_DiscardPlane, vec4(rn_CameraEye, 1.0)) * dot(rn_DiscardPlane, vec4(rn_Position, 1.0));
        if (check > 0) discard;
    }
    rn_FragmentColor = vec4(vec3(0.5f) + (0.5f * rn_Normal), 1.0f);
}
