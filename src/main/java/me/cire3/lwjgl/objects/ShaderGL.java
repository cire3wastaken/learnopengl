package me.cire3.lwjgl.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.cire3.App.WORKING_DIRECTORY;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

public class ShaderGL {
    private int program = -1;
    private int vsh = -1;
    private int gsh = -1;
    private int fsh = -1;

    public ShaderGL(String vertex, String geometry, String fragment) {
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

        deleteShaders();
    }

    public ShaderGL(int program, int vsh, int gsh, int fsh) {
        this.program = program;
        this.vsh = vsh;
        this.fsh = fsh;
        this.gsh = gsh;
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

    public void setInt(String name, int value) {
        if (program != -1)
            glUniform1i(getUniform(name), value);
    }

    public void setFloat(String name, float value) {
        if (program != -1)
            glUniform1f(getUniform(name), value);
    }

    // FIXME this dont work idk why
    public int getUniform(String uniform) {
        return glGetUniformLocation(program, uniform);
    }

    public static String getShaderSource(String shaderName) {
        File file1 = new File(WORKING_DIRECTORY.getAbsolutePath() + "/resources/shaders/", shaderName);

        try (InputStream is = new FileInputStream(file1)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
