#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;

out vec3 finalColor;

uniform float u_offset;

void main()
{
    gl_Position = vec4(aPos.x + u_offset, aPos.y, aPos.z, 1.0);
    finalColor = aColor;
}