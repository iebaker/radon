#version 400

uniform vec3 rn_Color;

out vec4 rn_FragmentColor;

void main() {
    rn_FragmentColor = vec4(rn_Color, 1);
}