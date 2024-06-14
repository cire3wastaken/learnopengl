package me.cire3;

import me.cire3.lwjgl.objects.ProgramGL;

import java.io.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

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
        ProgramGL program = setupShaderProgram();

        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f,  0.5f, 0.0f
        };

        int[] indices = {
                0, 1, 2,
                1, 2, 3
        };

        int vao = glGenVertexArrays();
        int ebo = glGenBuffers();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        program.useProgram();
        program.deleteShaders();

        glBindVertexArray(vao);

        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

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
