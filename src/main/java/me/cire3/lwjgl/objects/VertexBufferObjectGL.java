package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBufferObjectGL extends ObjectGL {
    private int vboId = -1;

    private VertexBufferObjectGL(int vboId) {
        this.vboId = vboId;
    }

    public static VertexBufferObjectGL newVertexBufferObjectGL(FloatBuffer buffer) {
        int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        return new VertexBufferObjectGL(vboId);
    }

    public void bind() {
        if (vboId != -1)
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
    }

    public void update(IntBuffer buffer) {
        if (vboId != -1) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
    }

    @Override
    public void cleanup() {
        if (vboId != -1)
            glDeleteBuffers(vboId);
    }
}
