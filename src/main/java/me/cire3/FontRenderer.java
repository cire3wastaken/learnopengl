package me.cire3;

import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.VertexBufferObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderFontRendererProgramGL;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class FontRenderer {
    private static final Map<Font, FontRenderer> FONT_RENDERER_CACHE = new HashMap<>();
    public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    public static final String ALL_ASCII_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private static final int MARGIN_WIDTH = 4;
    private static final FloatBuffer VERTICES;

    static {
        VERTICES = BufferUtils.createFloatBuffer()
    }

    private final PipelineShaderFontRendererProgramGL shaderProgram;
    private final VertexArrayObjectGL vao;
    // need raw control over stuff
    private int instancesBufferId = -1;

    // set it to fill up ASCII first
    private Map<Character, Glyph> characterGlyphs = new HashMap<>(128);

    private FontRenderer(Font font, boolean antialias, boolean fractionalMetrics) {
        // -------------------- DUMMY STUFF --------------------
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dummyGraphics = (Graphics2D) dummy.getGraphics();
        FontMetrics dummyFontMetrics = dummyGraphics.getFontMetrics(font);

        // -------------------- GENERATE GLYPHS --------------------
        for (char c : ALL_ASCII_CHARS.toCharArray()) {
            Rectangle2D bounds = dummyFontMetrics.getStringBounds(c + "", dummyGraphics);

            BufferedImage image = new BufferedImage((int) Math.ceil(bounds.getWidth()) + MARGIN_WIDTH * 2,
                    (int) Math.ceil(bounds.getHeight()) + 0,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D charGraphics = (Graphics2D) image.getGraphics();
            charGraphics.setFont(font);

            // -------------------- CONFIGURE GLYPHS --------------------
            int width = image.getWidth();
            int height = image.getHeight();
            charGraphics.setColor(TRANSPARENT_COLOR);
            charGraphics.fillRect(0, 0, width, height);

            charGraphics.setColor(Color.WHITE);

            if (antialias) {
                charGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                charGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            charGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            charGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            // -------------------- DRAW GLYPH --------------------
            charGraphics.drawString(c + "", MARGIN_WIDTH, font.getSize());

            // -------------------- SAVE GLYPH --------------------
            TextureGL textureGL = TextureGL.newTexture("Glyph: {}".replace("{}", c + ""), image,
                    GL_TEXTURE_2D, true, false, new TextureGL.TextureParameterConfigurer() {
                        @Override
                        public void configure() {
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                        }
                    });

            Glyph glyph = new Glyph(textureGL, width, height);

            this.characterGlyphs.put(c, glyph);
        }

        // -------------------- OPENGL STUFF--------------------
        this.shaderProgram = PipelineShaderFontRendererProgramGL.create();
        this.shaderProgram.setupUniforms();
    }

    public static FontRenderer newFontRenderer(Font font, boolean antialias, boolean fractionalMetrics) {
        FontRenderer cachedFontRenderer = FONT_RENDERER_CACHE.get(font);
        if (cachedFontRenderer != null)
            return cachedFontRenderer;
        cachedFontRenderer = new FontRenderer(font, antialias, fractionalMetrics);
        FONT_RENDERER_CACHE.put(font, cachedFontRenderer);
        return cachedFontRenderer;
    }

    private static class Glyph {
        private TextureGL textureGL;
        private float width;
        private float height;

        public Glyph(TextureGL textureGL, float width, float height) {
            this.textureGL = textureGL;
            this.width = width;
            this.height = height;
        }

        public void draw(float x, float y) {
            textureGL.bind();

        }
    }
}