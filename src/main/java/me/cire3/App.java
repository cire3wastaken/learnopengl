package me.cire3;

import me.cire3.lwjgl.objects.ProgramGL;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengles.GLES20.*;
import static org.lwjgl.opengles.GLES30.*;

public class App {
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

        ProgramGL program = setupShaderProgram();
        program.deleteShaders();

        FloatBuffer vertices = BufferUtils.createFloatBuffer(9);
        vertices.put(new float[]{
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        });

        IntBuffer indices = BufferUtils.createIntBuffer(6);
        indices.put(new int[]{
                0, 1, 2,
                1, 2, 3
        });

        int vao = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            program.useProgram();
            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        program.deleteProgram();

        glfwTerminate();
    }

    public ProgramGL setupShaderProgram() {
        int vsh = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vsh, getShaderSource("vertex_shader.vsh"));
        glCompileShader(vsh);

        if (glGetShaderi(vsh, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n{}"
                    .replace("{}", glGetShaderInfoLog(vsh)));
            throw new RuntimeException("Failed to compile vertex shader!");
        }

        int fsh = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fsh, getShaderSource("fragment_shader.vsh"));
        glCompileShader(fsh);

        if (glGetShaderi(fsh, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n{}"
                    .replace("{}", glGetShaderInfoLog(fsh)));
            throw new RuntimeException("Failed to compile fragment shader!");
        }

        int program = glCreateProgram();
        glAttachShader(program, vsh);
        glAttachShader(program, fsh);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n{}"
                    .replace("{}", glGetProgramInfoLog(program)));
            throw new RuntimeException("Failed to link shader program!");
        }

        return new ProgramGL(program, vsh, fsh, -1);
    }

    private String getShaderSource(String shaderName) {
        File resource = new File("resources/shaders/{}".replace("{}", shaderName));
        try (InputStream is = new BufferedInputStream(new FileInputStream(resource))) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleInput(long window) {
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }

    public void framebufferSizeCallback(long window, int w, int h) {
        glViewport(0, 0, w, h);
    }
}
