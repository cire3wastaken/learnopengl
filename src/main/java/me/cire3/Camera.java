package me.cire3;

import org.joml.Matrix4f;

// todo
public class Camera {
    private float x;
    private float y;
    private float z;

    private float yaw;
    private float pitch;

    public Matrix4f getViewMatrix(Matrix4f viewMatrix) {
        viewMatrix.identity();
        viewMatrix.translate(x, y, z);
        viewMatrix.rotate(yaw, 1, 0, 0);
        viewMatrix.rotate(pitch, 0, 1, 0);
        return viewMatrix;
    }
}
