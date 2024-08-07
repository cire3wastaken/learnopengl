package me.cire3;

import me.cire3.lwjgl.ObjectGLManager;
import me.cire3.lwjgl.objects.ElementBufferObjectGL;
import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.VertexBufferObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderOldCoreProgramGL;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30C.*;

public class App {
    public static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

    private static App instance;
    private Renderer renderer;

    private final long window;

    private static float playerX;
    private static float playerY;
    private static float playerZ = -3.0F; // yes

    private static float yaw;
    private static float pitch;

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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FontRenderer fontRenderer = FontRenderer.newFontRenderer(new Font("Arial", Font.PLAIN, 14), true, true);

            float[] verticesData = {
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
                    0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                    0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                    -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

                    -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                    -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                    -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                    0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                    0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                    0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                    -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
            };

            FloatBuffer vertices = stack.floats(verticesData);

            int[] indicesData = {
                    0, 1, 3, // first triangle
                    1, 2, 3  // second triangle
            };
            IntBuffer indices = stack.ints(indicesData);

            PipelineShaderOldCoreProgramGL pipelineShaderOldCoreProgramGL = PipelineShaderOldCoreProgramGL.create();
            pipelineShaderOldCoreProgramGL.setupUniforms();

            TextureGL woodenBox = TextureGL.newTexture("wooden_box.png", GL_TEXTURE_2D, false);
            TextureGL awesomeFace = TextureGL.newTexture("awesome_face.png", GL_TEXTURE_2D, true);

            Matrix4f projectionMatrix = new Matrix4f();
            projectionMatrix.perspective((float) Math.toRadians(45.0f), 800.0F / 600.0F, 0.01F, 100.0F);

            Matrix4f viewMatrix = new Matrix4f();
//            viewMatrix.translate(0.0F, 0.0F, -3.0F);

            Matrix4f modelMatrix = new Matrix4f();
            final Vector3f modelMatrixRotationVector = new Vector3f(1.0F, 0.3F, 0.5F).normalize();
            // setup model matrix in frame

            Matrix4f pvMatrix = new Matrix4f();
            Matrix4f pvmMatrix = new Matrix4f();
            Matrix4f vmMatrix = new Matrix4f();

            Vector3f[] cubePositions = new Vector3f[]{
                    new Vector3f(0.0f, 0.0f, 0.0f),
                    new Vector3f(2.0f, 5.0f, -15.0f),
                    new Vector3f(-1.5f, -2.2f, -2.5f),
                    new Vector3f(-3.8f, -2.0f, -12.3f),
                    new Vector3f(2.4f, -0.4f, -3.5f),
                    new Vector3f(-1.7f, 3.0f, -7.5f),
                    new Vector3f(1.3f, -2.0f, -2.5f),
                    new Vector3f(1.5f, 2.0f, -2.5f),
                    new Vector3f(1.5f, 0.2f, -1.5f),
                    new Vector3f(-1.3f, 1.0f, -1.5f)
            };

            VertexBufferObjectGL vbo = VertexBufferObjectGL.newVertexBufferObjectGL(vertices);
            vbo.bind();
            vbo.loadData();

            VertexArrayObjectGL vao = VertexArrayObjectGL.newVertexArrayObject(indices);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            ElementBufferObjectGL ebo = vao.getEbo();

            pipelineShaderOldCoreProgramGL.bind();
            glUniform1i(pipelineShaderOldCoreProgramGL.getUniforms().u_texture1.getId(), 0);
            glUniform1i(pipelineShaderOldCoreProgramGL.getUniforms().u_texture2.getId(), 1);

            final FloatBuffer temporaryMatrixDataBuffer = stack.callocFloat(16);

            while (!glfwWindowShouldClose(window)) {
                handleInput(window);

                glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                glActiveTexture(GL_TEXTURE0);
                woodenBox.bind();
                glActiveTexture(GL_TEXTURE1);
                awesomeFace.bind();

                pipelineShaderOldCoreProgramGL.bind();
                viewMatrix.identity();
                viewMatrix.translate(playerX, playerY, playerZ);
                viewMatrix.rotate(yaw, 1, 0, 0);
                viewMatrix.rotate(pitch, 0, 1, 0);

                pvMatrix.identity();
                vmMatrix.identity();
                pvmMatrix.identity();

                projectionMatrix.mul(viewMatrix, pvMatrix);

                glEnable(GL_DEPTH_TEST);

                vao.bind();
                for (int i = 0; i < 10; i++) {
                    // calculate the model matrix for each object and pass it to shader before drawing
                    modelMatrix.identity();
                    modelMatrix.translate(cubePositions[i]);
                    float angle = 20.0f * i;
                    if (i % 3 == 0)
                        angle = (float) (glfwGetTime() * 25.0f) + 20.0f * i;
                    modelMatrix.rotate((float) Math.toRadians(angle), modelMatrixRotationVector);

                    viewMatrix.mul(modelMatrix, vmMatrix);
                    pvMatrix.mul(modelMatrix, pvmMatrix);

                    glUniformMatrix4fv(pipelineShaderOldCoreProgramGL.getUniforms().u_pvmMatrix.getId(), false, pvmMatrix.get(temporaryMatrixDataBuffer));

                    glDrawArrays(GL_TRIANGLES, 0, 36);
                }

                glDisable(GL_DEPTH_TEST);
//
//                fontRenderer.updateMatrix(pvMatrix);
//                fontRenderer.drawString("baugette", 0, 0, Color.WHITE.getRGB());

                glfwSwapBuffers(window);
                glfwPollEvents();
            }

            ObjectGLManager.cleanup();
            glfwTerminate();
        }
    }

    public void handleInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            playerZ += 0.1F;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            playerZ -= 0.1F;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            playerX += 0.1F;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            playerX -= 0.1F;
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS)
            playerY -= 0.1F;
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
            playerY += 0.1F;
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

    public Renderer getRenderer() {
        return renderer;
    }
}
