#version 330 core
out vec4 FragColor;

in vec2 v_texturePos;

uniform sampler2D u_texture1;
uniform sampler2D u_texture2;

void main()
{
    FragColor = mix(texture(u_texture1, v_texturePos), texture(u_texture2, v_texturePos), 0.2);
//    FragColor = texture(texture1, v_texturePos);
}