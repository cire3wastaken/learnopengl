#version 330 core

layout(location = 0) in vec3 a_position3f;
layout(location = 1) in vec4 a_color;
layout(location = 2) in vec2 a_texturePos;

out vec4 v_color;
out vec2 v_texturePos;

uniform mat4 u_pvmMatrix;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position3f, 1.0);
    v_color = a_color;
    v_texturePos = a_texturePos;
}