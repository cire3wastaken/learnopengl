package me.cire3;

import me.cire3.lwjgl.ObjectGLManager;
import me.cire3.lwjgl.objects.ProgramGL;
import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.UniformGL;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

        float[] verticesData = {
                // positions          // colors           // texture coords
                0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,   // top right
                0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,   // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,   // bottom left
                -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f    // top left
        };

        FloatBuffer vertices = BufferUtils.createFloatBuffer(verticesData.length);
        vertices.put(verticesData).flip();

        int[] indicesData = {
                0, 1, 3, // first triangle
                1, 2, 3  // second triangle
        };
        IntBuffer indices = BufferUtils.createIntBuffer(indicesData.length);
        indices.put(indicesData).flip();

        ProgramGL prog = ProgramGL.newProgram("vertex_shader.vsh", null, "fragment_shader.fsh");
        TextureGL woodenBox = TextureGL.newTexture("wooden_box.png", GL_TEXTURE_2D, false);
        TextureGL awesomeFace = TextureGL.newTexture("awesome_face.png", GL_TEXTURE_2D, true);


        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.perspective((float) Math.toRadians(55.0F), 800.0F / 600.0F, 0.1F, 10);

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.translate(0.0f, 0.0f, -3.0f);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.rotate((float) Math.toRadians(-55.0F), 1.0F, 0.0F, 0.0F);

        int vao = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        prog.useProgram();
        glUniform1i(prog.getUniform("texture1").getId(), 0);
        glUniform1i(prog.getUniform("texture2").getId(), 1);

        glUniformMatrix4fv(prog.getUniform("u_projectionMatrix").getId(), false, projectionMatrix.get(BufferUtils.createFloatBuffer(16)));
        glUniformMatrix4fv(prog.getUniform("u_viewMatrix").getId(), false, viewMatrix.get(BufferUtils.createFloatBuffer(16)));
        glUniformMatrix4fv(prog.getUniform("u_modelMatrix").getId(), false, modelMatrix.get(BufferUtils.createFloatBuffer(16)));

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(woodenBox.getTextureType(), woodenBox.getTextureId());
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(awesomeFace.getTextureType(), awesomeFace.getTextureId());

            prog.useProgram();
            glBindVertexArray(vao);
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
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }

    public void framebufferSizeCallback(long window, int w, int h) {
        glViewport(0, 0, w, h);
    }

    public static InputStream getInputStream(String filename) {
        File file = new File(WORKING_DIRECTORY.getAbsolutePath() + "/resources/" + filename);

        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
