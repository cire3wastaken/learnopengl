#version 330 core

precision lowp int;
precision mediump float;
precision mediump sampler2D;

in vec2 v_texCoord2f;
in vec4 v_color4f;

layout(location = 0) out vec4 output4f;

uniform sampler2D u_inputTexture;

void main() {
    output4f = texture(u_inputTexture, v_texCoord2f) * v_color4f;
    if(output4f.a < 0.004) {
        discard;
    }
}