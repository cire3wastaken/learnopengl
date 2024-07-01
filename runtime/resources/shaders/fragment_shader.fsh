#version 330 core
out vec4 FragColor;

in vec2 v_texturePos;

uniform sampler2D texture1;
uniform sampler2D texture2;

void main()
{
    FragColor = mix(texture(texture1, v_texturePos), texture(texture2, v_texturePos), 0.2);
//    FragColor = texture(texture1, v_texturePos);
}