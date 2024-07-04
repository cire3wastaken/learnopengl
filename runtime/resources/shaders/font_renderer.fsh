#version 330 core

in vec4 v_color;
in vec2 v_texturePos;

layout (location = 0) out vec4 colorOutput;

uniform sampler2D u_texture;

void main() {
    // texture atlas is transparent/white, so we only color the white ones
    colorOutput = texture(u_texture, v_texturePos) * v_color;
    if (colorOutput.a < 0.004) {
        discard;
    }
}