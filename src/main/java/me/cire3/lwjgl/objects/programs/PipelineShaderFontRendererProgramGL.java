package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class PipelineShaderFontRendererProgramGL extends IProgramGL<PipelineShaderFontRendererProgramGL.UniformsGL> {
    public PipelineShaderFontRendererProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        super(program, vsh, gsh, fsh, uniforms);
    }

    public static PipelineShaderFontRendererProgramGL create() {
        return IProgramGL.newProgramGL("font_renderer.vsh", null,
                "font_renderer.fsh", new UniformsGL(), PipelineShaderFontRendererProgramGL.class);
    }

    public static class UniformsGL implements IProgramUniformsGL<PipelineShaderFontRendererProgramGL> {
        public UniformGL u_pvmMatrix;
        public UniformGL u_texture;

        @Override
        public void setupUniforms(PipelineShaderFontRendererProgramGL prog) {
            u_texture = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_texture"));
            u_pvmMatrix = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_pvmMatrix"));
        }
    }
}
