package me.cire3;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

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
        while (!glfwWindowShouldClose(window)) {
            handleInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

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
