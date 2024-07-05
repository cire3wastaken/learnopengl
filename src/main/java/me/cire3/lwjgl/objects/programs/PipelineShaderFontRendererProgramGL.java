package me.cire3.lwjgl.objects.programs;

import me.cire3.lwjgl.objects.IProgramGL;
import me.cire3.lwjgl.objects.IProgramUniformsGL;
import me.cire3.lwjgl.objects.UniformGL;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;

public class PipelineShaderFontRendererProgramGL extends IProgramGL<PipelineShaderFontRendererProgramGL.UniformsGL> {
    public PipelineShaderFontRendererProgramGL(int program, int vsh, int gsh, int fsh, IProgramUniformsGL uniforms) {
        super(program, vsh, gsh, fsh, uniforms);
    }

    public static PipelineShaderFontRendererProgramGL create() {
        return IProgramGL.newProgramGL("font_renderer.vsh", null,
                "font_renderer.fsh", new UniformsGL(), PipelineShaderFontRendererProgramGL.class);
    }

    public static class UniformsGL implements IProgramUniformsGL<PipelineShaderFontRendererProgramGL> {
        public UniformGL u_matrixTransform;
        public UniformGL u_charSize2f;
        public UniformGL u_charCoordSize2f;
        public UniformGL u_inputTexture;

        @Override
        public void setupUniforms(PipelineShaderFontRendererProgramGL prog) {
            u_matrixTransform = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_matrixTransform"));
            u_charSize2f = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_charSize2f"));
            u_charCoordSize2f = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_charCoordSize2f"));
            u_inputTexture = new UniformGL(glGetUniformLocation(prog.getProgramId(), "u_inputTexture"));

            glUniform1i(u_inputTexture.getId(), 0);
        }
    }
}
