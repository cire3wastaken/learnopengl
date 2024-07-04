package me.cire3.lwjgl;

public abstract class ObjectGL {
    public ObjectGL() {
        ObjectGLManager.objects.add(this);
    }

    public abstract void cleanup();
}
