package me.cire3.lwjgl;

public abstract class ObjectGL {
    public ObjectGL() {
        ObjectGLManager.objects.add(this);
    }

    public final void cleanup() {
        ObjectGLManager.objects.remove(this);
        cleanup0();
    }

    protected abstract void cleanup0();
}
