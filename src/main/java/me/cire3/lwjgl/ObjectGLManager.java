package me.cire3.lwjgl;

import java.util.ArrayList;
import java.util.List;

public class ObjectGLManager {
    public static List<ObjectGL> objects = new ArrayList<>();

    public static void cleanup() {
        ObjectGL gl = objects.get(0);
        while (gl != null) {
            gl.cleanup();
            gl = objects.get(0);
        }
        objects.clear();
    }
}
