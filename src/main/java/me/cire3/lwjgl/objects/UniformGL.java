package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

public class UniformGL extends ObjectGL {
    private int uniform = -1;

    public UniformGL(int uniform) {
        this.uniform = uniform;
    }

    public int getId() {
        if (uniform == -1)
            throw new RuntimeException("Uniform not set");
        return uniform;
    }
}
