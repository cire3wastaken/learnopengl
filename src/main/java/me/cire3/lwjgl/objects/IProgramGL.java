package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static me.cire3.App.getInputStream;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

public abstract class IProgramGL<T extends IProgramUniformsGL> extends ObjectGL {
    private int program = -1;
    private int vsh = -1;
    private int gsh = -1;
    private int fsh = -1;
    private T uniforms = null;

    private boolean hasSetupUniforms;

    protected static String getShaderSource(String shaderName) {
        try (InputStream is = getInputStream("shaders/" + shaderName)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupSelf(String vertex, String geometry, String fragment, T uniforms) {
        if (vertex != null){
            vsh = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vsh, getShaderSource(vertex));
            glCompileShader(vsh);

            if (glGetShaderi(vsh, GL_COMPILE_STATUS) == GL_FALSE) {
                System.out.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n{}"
                        .replace("{}", glGetShaderInfoLog(vsh)));
                throw new RuntimeException("Failed to compile vertex shader!");
            }
        }

        if (geometry != null) {
            gsh = glCreateShader(GL_GEOMETRY_SHADER);
            glShaderSource(gsh, getShaderSource(geometry));
            glCompileShader(gsh);

            if (glGetShaderi(gsh, GL_COMPILE_STATUS) == GL_FALSE) {
                System.out.println("ERROR::SHADER::GEOMETRY::COMPILATION_FAILED\n{}"
                        .replace("{}", glGetShaderInfoLog(gsh)));
                throw new RuntimeException("Failed to compile geometry shader!");
            }
        }

        if (fragment != null){
            fsh = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fsh, getShaderSource(fragment));
            glCompileShader(fsh);

            if (glGetShaderi(fsh, GL_COMPILE_STATUS) == GL_FALSE) {
                System.out.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n{}"
                        .replace("{}", glGetShaderInfoLog(fsh)));
                throw new RuntimeException("Failed to compile fragment shader!");
            }
        }

        program = glCreateProgram();
        if (vsh != -1)
            glAttachShader(program, vsh);
        if (gsh != -1)
            glAttachShader(program, gsh);
        if (fsh != -1)
            glAttachShader(program, fsh);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n{}"
                    .replace("{}", glGetProgramInfoLog(program)));
            throw new RuntimeException("Failed to link shader program!");
        }

        deleteShaders();

        this.uniforms = uniforms;
    }

    public void setupUniforms() {
        if (uniforms != null) {
            useProgram();
            uniforms.setupUniforms(this);
            hasSetupUniforms = true;
        }
    }

    public void useProgram() {
        if (program != -1)
            glUseProgram(program);
    }

    public void deleteShaders() {
        if (vsh != -1) {
            glDeleteShader(vsh);
            vsh = -1;
        }
        if (gsh != -1) {
            glDeleteShader(gsh);
            gsh = -1;
        }
        if (fsh != -1) {
            glDeleteShader(fsh);
            fsh = -1;
        }
    }

    public void deleteProgram() {
        if (program != -1) {
            glDeleteProgram(program);
            program = -1;
        }
    }

    @Override
    public void cleanup() {
        deleteShaders();
        deleteProgram();
    }

    public int getProgramId() {
        return program;
    }

    public int getVshId() {
        return vsh;
    }

    public int getGshId() {
        return gsh;
    }

    public int getFshId() {
        return fsh;
    }

    public T getUniforms() {
        if (hasSetupUniforms)
            return uniforms;
        throw new IllegalArgumentException("Tried to get uniforms when uniforms haven't been setup!");
    }
}
