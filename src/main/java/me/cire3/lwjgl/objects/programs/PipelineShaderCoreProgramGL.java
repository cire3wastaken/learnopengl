package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class PipelineShaderCoreProgramGL extends IProgramGL<PipelineShaderCoreProgramGL.ProgramUniformsGL> {
    public static PipelineShaderCoreProgramGL create() {
        PipelineShaderCoreProgramGL program = new PipelineShaderCoreProgramGL();
        program.setupSelf("vertex_shader.vsh", null, "fragment_shader.fsh", new ProgramUniformsGL());
        return program;
    }

    public static class ProgramUniformsGL implements IProgramUniformsGL {
        public UniformGL u_texture1;
        public UniformGL u_texture2;
        public UniformGL u_pvmMatrix;

        @Override
        public void setupUniforms(IProgramGL prog) {
            u_texture1 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture1"));
            u_texture2 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture2"));
            u_pvmMatrix = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_pvmMatrix"));
        }
    }
}
