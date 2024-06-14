package me.cire3.lwjgl.objects;

public class UniformGL {
    private int uniform = -1;

    public UniformGL(int uniform) {
        this.uniform = uniform;
    }

    public int getID() {
        if (uniform == -1)
            throw new RuntimeException("Uniform not set");
        return uniform;
    }
}
