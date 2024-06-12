package me.cire3;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello LWJGL! - cire3");
        initGlfw();
        App.getInstance().run();
    }

    public static void initGlfw() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new RuntimeException("Failed to initialize GLFW!");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(800, 600, "LearnOpenGl", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create Window object!");
        }

        new App(window);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSetFramebufferSizeCallback(window, (window0, w, h) ->
                App.getInstance().framebufferSizeCallback(window0, w, h));
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }
}