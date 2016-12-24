#version 400

in vec3 rn_Normal;

out vec4 rn_FragmentColor;

void main() {
    rn_FragmentColor = vec4(vec3(0.5f) + (0.5f * rn_Normal), 1.0f);
}
