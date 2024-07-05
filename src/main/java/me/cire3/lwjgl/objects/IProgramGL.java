package me.cire3.lwjgl.objects;

import me.cire3.lwjgl.ObjectGL;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static me.cire3.App.getInputStream;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

public abstract class IProgramGL<T extends IProgramUniformsGL> extends ObjectGL {
    protected int program = -1;
    protected int vsh = -1;
    protected int gsh = -1;
    protected int fsh = -1;
    protected T uniforms = null;

    protected boolean hasSetupUniforms;

    protected boolean detachedVsh;
    protected boolean detachedGsh;
    protected boolean detachedFsh;

    @SuppressWarnings("unchecked")
    public IProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        this.program = program;
        this.vsh = vsh;
        this.gsh = gsh;
        this.fsh = fsh;
        this.uniforms = (T) uniforms;
    }

    protected static String getShaderSource(String shaderName) {
        try (InputStream is = getInputStream("shaders/" + shaderName)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <P extends IProgramGL> P newProgramGL(String vertex, String geometry, String fragment, IProgramUniformsGL<P> uniforms, Class<P> clazz) {
        int vsh = -1, gsh = -1, fsh = -1;

        if (vertex != null) {
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

        if (fragment != null) {
            fsh = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fsh, getShaderSource(fragment));
            glCompileShader(fsh);

            if (glGetShaderi(fsh, GL_COMPILE_STATUS) == GL_FALSE) {
                System.out.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n{}"
                        .replace("{}", glGetShaderInfoLog(fsh)));
                throw new RuntimeException("Failed to compile fragment shader!");
            }
        }

        int program = glCreateProgram();
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

        try {
            P prog = clazz.getDeclaredConstructor(int.class, int.class, int.class, int.class, IProgramUniformsGL.class)
                    .newInstance(program, vsh, gsh, fsh, uniforms);

            prog.detachShaders();
            prog.deleteShaders();
            return prog;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setupUniforms() {
        if (uniforms != null) {
            if (program == -1)
                throw new IllegalArgumentException("Program is not valid!");
            glUseProgram(program);
            uniforms.setupUniforms(this);
            hasSetupUniforms = true;
        }
    }

    public void bind() {
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

    public void detachShaders() {
        if (vsh != -1 && !detachedVsh) {
            glDetachShader(program, vsh);
            detachedVsh = true;
        }
        if (gsh != -1 && !detachedGsh) {
            glDetachShader(program, gsh);
            detachedGsh = true;
        }
        if (fsh != -1 && !detachedFsh) {
            glDetachShader(program, fsh);
            detachedFsh = true;
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
