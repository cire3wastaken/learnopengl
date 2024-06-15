package me.cire3;

import me.cire3.lwjgl.ObjectGLManager;
import me.cire3.lwjgl.objects.ProgramGL;
import me.cire3.lwjgl.objects.UniformGL;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30C.*;

public class App {
    public static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

    private static App instance;

    private final long window;

    public App(long window) {
        if (instance != null)
            throw new IllegalStateException("Instance already set!");
        instance = this;
        this.window = window;
    }

    public static App getInstance() {
        if (instance == null)
            throw new IllegalStateException("Instance not set!");
        return instance;
    }

    public void run() {
        GL.createCapabilities();

        ProgramGL prog = ProgramGL.newProgram("vertex_shader.vsh", null, "fragment_shader.fsh");

        float[] verticesData = {
                // vertices                  color
                0.5f, -0.5f, 0.0f,       1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, 0.0f,     0.0f, 1.0f, 0.0f,
                0f, 0.5f, 0.0f,         0.0f, 0.0f, 1.0f
        };

        FloatBuffer vertices = BufferUtils.createFloatBuffer(verticesData.length);
        vertices.put(verticesData).flip();

        int[] indicesData = {
                0, 1, 2,
        };
        IntBuffer indices = BufferUtils.createIntBuffer(indicesData.length);
        indices.put(indicesData).flip();

        int vao = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            prog.useProgram();
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);

        ObjectGLManager.cleanup();
        glfwTerminate();
    }

    public void handleInput(long window) {
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }

    public void framebufferSizeCallback(long window, int w, int h) {
        glViewport(0, 0, w, h);
    }
}
