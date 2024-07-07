#version 330 core

in vec4 v_color4f;
in vec2 v_texPos2f;

layout(location = 0) out vec4 fragColor;

uniform sampler2D u_texture;

void main() {
    vec4 textureColor = texture(u_texture, v_texPos2f);
    fragColor = v_color4f * textureColor;
}