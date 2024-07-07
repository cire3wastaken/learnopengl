package me.cire3;

import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.VertexBufferObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderFontRendererProgramGL;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class FontRenderer {
    private static final Map<Font, FontRenderer> FONT_RENDERER_CACHE = new HashMap<>();
    public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    public static final String ALL_ASCII_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private static final int MARGIN_WIDTH = 4;
    private static final FloatBuffer VERTICES;

    static {
        VERTICES = BufferUtils.createFloatBuffer(18);
        VERTICES.put(new float[]{
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 1.0f, 0.0f,
        });
        VERTICES.flip();
    }

    private final PipelineShaderFontRendererProgramGL shaderProgram;
    private final VertexArrayObjectGL vao;
    // need raw control over stuff
    private int instancesBufferId = -1;
    private ByteBuffer fontDataBuffer;

    // set it to fill up ASCII first
    private Map<Character, TextureGL> characterGlyphs = new HashMap<>(128);

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
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                        }
                    });

            this.characterGlyphs.put(c, textureGL);
        }

        // -------------------- OPENGL STUFF--------------------
        this.fontDataBuffer = BufferUtils.createByteBuffer

        this.shaderProgram = PipelineShaderFontRendererProgramGL.create();
        this.shaderProgram.setupUniforms();

        this.instancesBufferId = glGenBuffers();

        this.vao = VertexArrayObjectGL.newVertexArrayObjectWithoutEBO();
        this.vao.bind();

        VertexBufferObjectGL vbo = VertexBufferObjectGL.newVertexBufferObjectGL(VERTICES);
        vbo.bind();
        vbo.loadData();

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glVertexAttribDivisor(0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, instancesBufferId);
        glBufferData(GL_ARRAY_BUFFER, , GL_STATIC_DRAW);
    }

    public static FontRenderer newFontRenderer(Font font, boolean antialias, boolean fractionalMetrics) {
        FontRenderer cachedFontRenderer = FONT_RENDERER_CACHE.get(font);
        if (cachedFontRenderer != null)
            return cachedFontRenderer;
        cachedFontRenderer = new FontRenderer(font, antialias, fractionalMetrics);
        FONT_RENDERER_CACHE.put(font, cachedFontRenderer);
        return cachedFontRenderer;
    }
}