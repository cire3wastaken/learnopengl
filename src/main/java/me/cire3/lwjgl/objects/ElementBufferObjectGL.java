package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;
import me.cire3.lwjgl.ObjectGLManager;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class ElementBufferObjectGL extends ObjectGL {
    private int eboId = -1;
    private IntBuffer buffer;

    private ElementBufferObjectGL(int eboId, IntBuffer buffer) {
        this.eboId = eboId;
        this.buffer = buffer;
    }

    public static ElementBufferObjectGL newIncompleteElementBufferObject() {
        return newElementBufferObject(null);
    }

    public static ElementBufferObjectGL newElementBufferObject(@Nullable IntBuffer indices) {
        int eboId = glGenBuffers();
        return new ElementBufferObjectGL(eboId, indices);
    }

    public void bind() {
        if (eboId != -1) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        }
    }

    public void update(IntBuffer buffer) {
        if (eboId != -1) {
            this.buffer = buffer;
            bind();
            loadData();
        }
    }

    public void loadData() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    @Override
    public void cleanup() {
        if (eboId != -1)
            glDeleteBuffers(eboId);
        ObjectGLManager.objects.remove(this);
    }
}
