package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class PipelineShaderOldCoreProgramGL extends IProgramGL<PipelineShaderOldCoreProgramGL.ProgramUniformsGL> {
    public PipelineShaderOldCoreProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        super(program, vsh, gsh, fsh, uniforms);
    }

    public static PipelineShaderOldCoreProgramGL create() {
        return IProgramGL.newProgramGL("old_core.vsh", null, "old_core.fsh", new ProgramUniformsGL(),
                PipelineShaderOldCoreProgramGL.class);
    }

    public static class ProgramUniformsGL implements IProgramUniformsGL<PipelineShaderOldCoreProgramGL> {
        public UniformGL u_texture1;
        public UniformGL u_texture2;
        public UniformGL u_pvmMatrix;

        @Override
        public void setupUniforms(PipelineShaderOldCoreProgramGL prog) {
            u_texture1 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture1"));
            u_texture2 = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture2"));
            u_pvmMatrix = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_pvmMatrix"));
        }
    }
}
