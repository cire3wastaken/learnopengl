package me.cire3.lwjgl.objects;

import static org.lwjgl.opengl.GL20C.*;

public class ProgramGL {
    private int program = -1;
    private int vsh = -1;
    private int fsh = -1;
    private int gsh = -1;

    public ProgramGL(int program, int vsh, int fsh, int gsh) {
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
        if (fsh != -1) {
            glDeleteShader(fsh);
            fsh = -1;
        }
        if (gsh != -1) {
            glDeleteShader(gsh);
            gsh = -1;
        }
    }

    public void deleteProgram() {
        if (program != -1) {
            glDeleteProgram(program);
            program = -1;
        }
    }
}
