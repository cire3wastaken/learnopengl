package me.cire3;

import me.cire3.lwjgl.objects.ProgramGL;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30C.*;

public class App {
    private static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

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

        float[] verticesData1 = {
                0.5f,  0.5f, 0.0f,
                1f, -0.5f, 0.0f,
                0f, -0.5f, 0.0f,
        };

        float[] verticesData2 = {
                -0.5f, 0.5f, 0.0f,
                0f, -0.5f, 0.0f,
                -1f,  -0.5f, 0.0f
        };

        FloatBuffer vertices1 = BufferUtils.createFloatBuffer(verticesData1.length);
        vertices1.put(verticesData1).flip();

        FloatBuffer vertices2 = BufferUtils.createFloatBuffer(verticesData2.length);
        vertices2.put(verticesData2).flip();

        int[] indicesData = {
                0, 1, 2,
        };

        IntBuffer indices = BufferUtils.createIntBuffer(indicesData.length);
        indices.put(indicesData).flip();

        int vao1 = glGenVertexArrays();
        int vao2 = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo1 = glGenBuffers();
        int vbo2 = glGenBuffers();

        glBindVertexArray(vao1);

        glBindBuffer(GL_ARRAY_BUFFER, vbo1);
        glBufferData(GL_ARRAY_BUFFER, vertices1, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);


        glBindVertexArray(vao2);

        glBindBuffer(GL_ARRAY_BUFFER, vbo2);
        glBufferData(GL_ARRAY_BUFFER, vertices2, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            program.useProgram();
            glBindVertexArray(vao1);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glBindVertexArray(vao2);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(vao1);
        glDeleteBuffers(vbo1);
        glDeleteBuffers(ebo);
        program.deleteProgram();

        glfwTerminate();
    }

    public static ProgramGL setupShaderProgram() {
        int vsh = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vsh, getShaderSource("vertex_shader.vsh"));
        glCompileShader(vsh);

        if (glGetShaderi(vsh, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n{}"
                    .replace("{}", glGetShaderInfoLog(vsh)));
            throw new RuntimeException("Failed to compile vertex shader!");
        }

        int fsh = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fsh, getShaderSource("fragment_shader.fsh"));
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

        ProgramGL programGL = new ProgramGL(program, vsh, fsh, -1);
        programGL.deleteShaders();
        return programGL;
    }

    private static String getShaderSource(String shaderName) {
        File file1 = new File(WORKING_DIRECTORY.getAbsolutePath() + "/resources/shaders/", shaderName);

        try (InputStream is = new FileInputStream(file1)) {
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
