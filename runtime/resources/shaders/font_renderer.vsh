#version 330 core

precision lowp int;
precision highp float;
precision mediump sampler2D;

// position to draw to
layout(location = 0) in vec2 a_position2f;
// color of the character
layout(location = 1) in vec4 c_color4f;
// uv of the character texture
layout(location = 2) in vec2 c_pos2f;

out vec2 v_texCoord2f;
out vec4 v_color4f;

uniform mat4 u_matrixTransform;

void main() {
    v_texCoord2f = c_pos;
    v_color4f = c_color4f;

    gl_Position = u_matrixTransform * vec4(a_position2f, 0.0, 1.0);
}