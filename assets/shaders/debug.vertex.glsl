#version 330 core

layout(location = 0) in vec3 a_position;
layout(location = 1) in vec4 a_color;

uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_color;

out vec4 v_color;

void main() {
    gl_Position = u_projTrans * u_worldTrans * vec4(a_position, 1.0);
    v_color = u_color;
}
