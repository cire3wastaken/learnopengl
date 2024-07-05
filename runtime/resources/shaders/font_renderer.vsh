#version 330 core

layout (location = 0) in vec3 a_position3f;

layout (location = 1) in vec4 a_color;
layout (location = 2) in vec2 a_texturePos;
layout (location = 3) in vec2 a_charPos;

out vec4 v_color;
out vec2 v_texturePos;

uniform mat4 u_pvmMatrix;

void main() {
    v_color = a_color.bgra;
    float shadowBit = a_position3f.z;
    float boldBit = shadowBit >= 0.5 ? 1.0 : 0.0;
    shadowBit -= boldBit * 0.5;
    v_color.rgb *= (1.0 - shadowBit * 3.0);
    v_texturePos = (a_texturePos + a_position3f.xy);
    vec2 pos2d = a_charPos + vec2(shadowBit * 4.0);
    pos2d += a_position3f.xy;
    pos2d.x += boldBit;
    float italicBit = v_color.a >= 0.5 ? 2.0 : 0.0;
    v_color.a -= italicBit * 0.25;
    pos2d.x -= (a_position3f.y - 0.5) * italicBit;
    v_color.a *= 2.0;
    gl_Position = u_pvmMatrix * vec4(pos2d, 0.0, 1.0);
}