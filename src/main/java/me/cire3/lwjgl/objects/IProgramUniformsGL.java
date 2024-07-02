package me.cire3.lwjgl.objects;

public interface IProgramUniformsGL<T extends IProgramGL> {
    void setupUniforms(T prog);
}
