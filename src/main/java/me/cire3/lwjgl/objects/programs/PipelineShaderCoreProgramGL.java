package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class PipelineShaderCoreProgramGL extends IProgramGL<PipelineShaderCoreProgramGL.UniformsGL> {
    public PipelineShaderCoreProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        super(program, vsh, gsh, fsh, uniforms);
    }

    public static PipelineShaderCoreProgramGL create() {
        return IProgramGL.newProgramGL("core.fsh", null, "core.vsh",
                new UniformsGL(), PipelineShaderCoreProgramGL.class);
    }

    public static class UniformsGL implements IProgramUniformsGL<PipelineShaderCoreProgramGL> {
        public UniformGL u_pvm;
        public UniformGL u_texture;

        @Override
        public void setupUniforms(PipelineShaderCoreProgramGL prog) {
            u_pvm = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_pvm"));
            u_texture = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture"));
        }
    }
}
