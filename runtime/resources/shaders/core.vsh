#version 330 core

layout(position = 0) in vec3 a_pos3f;
layout(position = 1) in vec4 a_color4f;
layout(position = 2) in vec2 a_texPos2f;

out vec4 v_color4f;
out vec2 v_texPos2f;

uniform mat4 u_pvm;

void main() {
    v_color4f = a_color4f;
    v_texPos2f = a_texPos2f;
    gl_Position = u_pvm * vec4(a_pos3f, 1.0);
}