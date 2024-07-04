package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBufferObjectGL extends ObjectGL {
    private int vboId = -1;
    private FloatBuffer buffer;

    private VertexBufferObjectGL(int vboId, FloatBuffer buffer) {
        this.vboId = vboId;
        this.buffer = buffer;
    }

    public static VertexBufferObjectGL newVertexBufferObjectGL(FloatBuffer buffer) {
        int vboId = glGenBuffers();
        return new VertexBufferObjectGL(vboId, buffer);
    }

    public void bind() {
        if (vboId != -1)
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
    }

    public void update(FloatBuffer buffer) {
        if (vboId != -1) {
            this.buffer = buffer;
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
    }

    public void loadData() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    @Override
    public void cleanup() {
        if (vboId != -1)
            glDeleteBuffers(vboId);
    }
}
