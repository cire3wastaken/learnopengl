package me.cire3.lwjgl;

import java.util.ArrayList;
import java.util.List;

public class ObjectGLManager {
    public static List<ObjectGL> objects = new ArrayList<>();

    public static void cleanup() {
        for (ObjectGL obj : objects) {
            obj.cleanup();
        }
        objects.clear();
    }
}
