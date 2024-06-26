#version 330 core
out vec4 FragColor;

in vec3 v_color;
in vec2 v_texturePos;

uniform sampler2D texture1;

void main()
{
    FragColor = texture(texture1, v_texturePos);
}