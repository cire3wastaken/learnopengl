#version 330 core
in vec3 finalColor;

out vec4 FragColor;

void main()
{
    FragColor = vec4(finalColor, 1.0f);
}