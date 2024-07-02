package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class PipelineShaderCoreProgramGL extends IProgramGL<PipelineShaderCoreProgramGL.ProgramUniformsGL> {
    public PipelineShaderCoreProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        super(program, vsh, gsh, fsh, uniforms);
    }

    public static PipelineShaderCoreProgramGL create() {
        return IProgramGL.makeSelf("core.vsh", null, "core.fsh", new ProgramUniformsGL(), PipelineShaderCoreProgramGL.class);
    }

    public static class ProgramUniformsGL implements IProgramUniformsGL<PipelineShaderCoreProgramGL> {
        public UniformGL u_texture1;
        public UniformGL u_texture2;
        public UniformGL u_pvmMatrix;

        @Override
        public void setupUniforms(PipelineShaderCoreProgramGL prog) {
            if (prog.getProgramId() == -1)
                throw new IllegalArgumentException("Program is not valid!");
            if (prog.getVshId() == -1)
                throw new IllegalArgumentException("Program is missing vertex shader!");
            if (prog.getFshId() == -1)
                throw new IllegalArgumentException("Program is missing fragment shader!");

            u_texture1 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture1"));
            u_texture2 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture2"));
            u_pvmMatrix = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_pvmMatrix"));
        }
    }
}
