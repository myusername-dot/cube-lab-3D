#version 330 core

layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec4 a_Color;

uniform mat4 u_projectionView;

out vec4 v_Color;

void main() {
    // Инвертируем Y-координату
    gl_Position = u_projectionView * vec4(a_Position.x, -a_Position.y, a_Position.z, 1.0);
    v_Color = a_Color;
}

//#version 330 core

//layout(location = 0) in vec3 a_Position;
//layout(location = 1) in vec4 a_Color;

//uniform mat4 u_projectionView;
//uniform mat4 u_worldTrans;

//out vec4 v_Color;

//void main() {
//    // Инвертируем Y-координату
//    vec4 worldPosition = u_worldTrans * vec4(a_Position.x, -a_Position.y, a_Position.z, 1.0);
//    gl_Position = u_projectionView * worldPosition;
//    v_Color = a_Color;
//}
