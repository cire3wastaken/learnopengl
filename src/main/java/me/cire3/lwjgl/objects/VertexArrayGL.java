package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

public class VertexArrayGL extends ObjectGL {
    private int vaoId = -1;

    private VertexArrayGL(int vaoId) {
        this.vaoId = vaoId;
    }

    public void use() {

    }

    public int getId() {
        if (vaoId == -1)
            throw new IllegalStateException("Vertex Array Object ID not set!");
        return vaoId;
    }
}
