package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class ElementBufferObjectGL extends ObjectGL {
    private int eboId = -1;
    private IntBuffer buffer;

    private ElementBufferObjectGL(int eboId) {
        this.eboId = eboId;
    }

    public static ElementBufferObjectGL newElementBufferObject(IntBuffer buffer) {
        int eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        return new ElementBufferObjectGL(eboId);
    }

    public void bind() {
        if (eboId != -1)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
    }

    public void update(IntBuffer buffer) {
        if (eboId != -1) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
    }

    @Override
    public void cleanup() {
        if (eboId != -1)
            glDeleteBuffers(eboId);
    }
}
