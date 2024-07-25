package me.cire3;

import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.VertexBufferObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderCoreProgramGL;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Renderer {
    private static final Renderer theRenderer = new Renderer();

    private VertexArrayObjectGL vao;
    private VertexBufferObjectGL vbo;
    private PipelineShaderCoreProgramGL program;

    private ByteBuffer buffer;
    private int numVertices;
    private boolean drawing;
    private final int stride = 3 * Integer.BYTES + 4 * Byte.BYTES + 2 * Integer.BYTES;

    private Renderer() {
        this.setupShaderProgram();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void begin() {
        if (drawing) {
            throw new IllegalStateException("Renderer is already drawing!");
        }
        drawing = true;
        numVertices = 0;
    }

    public void end() {
        if (!drawing) {
            throw new IllegalStateException("Renderer isn't drawing!");
        }
        drawing = false;
        flush();
    }

    public Renderer pos(int x, int y, int z) {
        buffer.putInt(numVertices * stride, x);
        buffer.putInt(numVertices * stride + 1, y);
        buffer.putInt(numVertices * stride + 2, z);
        return this;
    }

    public Renderer color(int r, int g, int b, int a) {
        buffer.put(numVertices * stride + 3, (byte) (r & 0xFF));
        buffer.put(numVertices * stride + 4, (byte) (g & 0xFF));
        buffer.put(numVertices * stride + 5, (byte) (b & 0xFF));
        buffer.put(numVertices * stride + 6, (byte) (a & 0xFF));
        return this;
    }

    public Renderer texPos(int u, int v) {
        buffer.putInt(numVertices * stride + 7, u);
        buffer.putInt(numVertices * stride + 8, v);
        return this;
    }

    public void endVertex() {
        numVertices++;
    }

    public void flush() {
        if (numVertices > 0) {
            buffer.flip();

            if (vao != null) {
                vao.bind();
            } else {
                throw new IllegalStateException("Drawing without a Vertex Array Object!");
            }
            program.bind();

            vbo.bind();
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);

            glDrawArrays(GL_TRIANGLES, 0, numVertices);

            buffer.clear();
            numVertices = 0;
        }
    }

    public void dispose() {
        MemoryUtil.memFree(buffer);

        if (vao != null)
            vao.cleanup();
        vbo.cleanup();
        program.cleanup();
    }

    private void setupShaderProgram() {
        this.vao = VertexArrayObjectGL.newIncompleteVertexArrayObject();
        this.vao.bind();

        this.vbo = VertexBufferObjectGL.newIncompleteVertexBufferObjectGL();
        this.vbo.bind();

        glVertexAttribPointer(0, 3, GL_UNSIGNED_INT, false, 3 * Integer.BYTES + 4 * Byte.BYTES + 2 * Integer.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, false, 3 * Integer.BYTES + 4 * Byte.BYTES + 2 * Integer.BYTES, 3 * Integer.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_UNSIGNED_INT, false, 3 * Integer.BYTES + 4 * Byte.BYTES + 2 * Integer.BYTES, 3 * Integer.BYTES + 4 * Byte.BYTES);
        glEnableVertexAttribArray(2);

        this.buffer = MemoryUtil.memAlloc(4096);

        long size = (long) this.buffer.capacity() * Float.BYTES;
        glBufferData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);

        this.numVertices = 0;
        this.drawing = false;

        this.program = PipelineShaderCoreProgramGL.create();
    }

    public static Renderer getInstance() {
        return theRenderer;
    }
}
