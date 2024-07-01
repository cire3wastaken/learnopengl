#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexturePos;

out vec2 v_texturePos;

uniform mat4 u_pvmMatrix;

void main()
{
    gl_Position = u_pvmMatrix * vec4(aPos, 1.0);
    v_texturePos = aTexturePos;
}