package me.cire3;

import me.cire3.lwjgl.ObjectGLManager;
import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderCoreProgramGL;
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
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
        };

        FloatBuffer vertices = BufferUtils.createFloatBuffer(verticesData.length);
        vertices.put(verticesData).flip();

        int[] indicesData = {
                0, 1, 3, // first triangle
                1, 2, 3  // second triangle
        };
        IntBuffer indices = BufferUtils.createIntBuffer(indicesData.length);
        indices.put(indicesData).flip();

        PipelineShaderCoreProgramGL pipelineShaderCoreProgramGL = PipelineShaderCoreProgramGL.create();
        pipelineShaderCoreProgramGL.setupUniforms();

        TextureGL woodenBox = TextureGL.newTexture("wooden_box.png", GL_TEXTURE_2D, false);
        TextureGL awesomeFace = TextureGL.newTexture("awesome_face.png", GL_TEXTURE_2D, true);

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.perspective((float) Math.toRadians(45.0f), 800.0F / 600.0F, 0.1F, 10.0F);

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.translate(0.0F, 0.0F, -3.0F);

        Matrix4f modelMatrix = new Matrix4f();
        // setup model matrix in frame

        Matrix4f pvMatrix = new Matrix4f();
        Matrix4f pvmMatrix = new Matrix4f();
        Matrix4f vmMatrix = new Matrix4f();

        Vector3f[] cubePositions = new Vector3f[] {
                new Vector3f(0.0f,  0.0f,  0.0f),
                new Vector3f( 2.0f,  5.0f, -15.0f),
                new Vector3f(-1.5f, -2.2f, -2.5f),
                new Vector3f(-3.8f, -2.0f, -12.3f),
                new Vector3f( 2.4f, -0.4f, -3.5f),
                new Vector3f(-1.7f,  3.0f, -7.5f),
                new Vector3f( 1.3f, -2.0f, -2.5f),
                new Vector3f( 1.5f,  2.0f, -2.5f),
                new Vector3f( 1.5f,  0.2f, -1.5f),
                new Vector3f(-1.3f,  1.0f, -1.5f)
        };

        int vao = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        pipelineShaderCoreProgramGL.useProgram();
        glUniform1i(pipelineShaderCoreProgramGL.getUniforms().u_texture1.getId(), 0);
        glUniform1i(pipelineShaderCoreProgramGL.getUniforms().u_texture2.getId(), 1);

        final FloatBuffer temporaryMatrixDataBuffer = BufferUtils.createFloatBuffer(16);

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(woodenBox.getTextureType(), woodenBox.getTextureId());
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(awesomeFace.getTextureType(), awesomeFace.getTextureId());

            pipelineShaderCoreProgramGL.useProgram();
            pvMatrix.identity();
            vmMatrix.identity();
            pvmMatrix.identity();

            projectionMatrix.mul(viewMatrix, pvMatrix);

            glEnable(GL_DEPTH_TEST);

            glBindVertexArray(vao);
            for (int i = 0; i < 10; i++)
            {
                // calculate the model matrix for each object and pass it to shader before drawing
                modelMatrix.identity();
                modelMatrix.translate(cubePositions[i]);
                modelMatrix.rotate((float) Math.toRadians(20.0F * i), 1.0F, 0.3F, 0.5F);
                
                viewMatrix.mul(modelMatrix, vmMatrix);
                pvMatrix.mul(modelMatrix, pvmMatrix);

                glUniformMatrix4fv(pipelineShaderCoreProgramGL.getUniforms().u_pvmMatrix.getId(), false, pvmMatrix.get(temporaryMatrixDataBuffer));

                glDrawArrays(GL_TRIANGLES, 0, 36);
            }

            glDisable(GL_DEPTH_TEST);
//            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

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
