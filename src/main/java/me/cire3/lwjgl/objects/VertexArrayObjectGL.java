package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;
import org.jetbrains.annotations.NotNull;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL30C.*;

public class VertexArrayObjectGL extends ObjectGL {
    private int vaoId = -1;
    private ElementBufferObjectGL ebo;

    private VertexArrayObjectGL(int vaoId, ElementBufferObjectGL ebo) {
        this.vaoId = vaoId;
        this.ebo = ebo;
    }

    public static VertexArrayObjectGL newVertexArrayObject(IntBuffer indices, @NotNull VertexArrayObjectConfigurer configurer) {
        int vao = glGenVertexArrays();

        glBindVertexArray(vao);
        ElementBufferObjectGL ebo = ElementBufferObjectGL.newElementBufferObject(indices);

        Objects.requireNonNull(configurer).configure();
        return new VertexArrayObjectGL(vao, ebo);
    }

    public void bind() {
        if (vaoId != -1)
            glBindVertexArray(vaoId);
    }

    public int getId() {
        if (vaoId == -1)
            throw new IllegalStateException("Vertex Array Object ID not set!");
        return vaoId;
    }

    public ElementBufferObjectGL getEbo() {
        return ebo;
    }

    @Override
    public void cleanup() {
        if (vaoId != -1)
            glDeleteVertexArrays(vaoId);
    }

    @FunctionalInterface
    public interface VertexArrayObjectConfigurer {
        void configure();
    }
}
