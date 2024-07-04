package me.cire3;

import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderFontRendererProgramGL;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;

public class FontRenderer {
    public static final Map<Font, FontRenderer> FONT_RENDERER_CACHE = new HashMap<>();
    public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    public static final String ALL_ASCII_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    public static final FloatBuffer VERTICES = BufferUtils.createFloatBuffer(18);

    static {
        VERTICES.put(new float[]{
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 1.0f, 0.0f
        });
        VERTICES.flip();
    }

    private PipelineShaderFontRendererProgramGL prog;
    private VertexArrayObjectGL vao;
    private Matrix4f pvmMatrix;
    private TextureGL internalFontTextureAtlas;

    private ByteBuffer fontDataBuffer;
    private int charactersDrawnToScreen;

    private final int asciiCharsLength;
    private FontMetrics fontMetrics;

    public FontRenderer(PipelineShaderFontRendererProgramGL prog, VertexArrayObjectGL vao, TextureGL internalFontTextureAtlas, FontMetrics fontMetrics) {
        this.prog = prog;
        this.vao = vao;
        this.pvmMatrix = new Matrix4f();
        this.internalFontTextureAtlas = internalFontTextureAtlas;

        this.fontDataBuffer = BufferUtils.createByteBuffer(6553 * 10);

        this.asciiCharsLength = this.fontMetrics.stringWidth(ALL_ASCII_CHARS);

        this.fontMetrics = fontMetrics;
    }

    public static FontRenderer newFontRenderer(Font font, boolean antialiasing, boolean fractionalMetrics) {
        FontRenderer cachedFontRenderer = FONT_RENDERER_CACHE.get(font);
        if (cachedFontRenderer != null)
            return cachedFontRenderer;

        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dummyGraphics = (Graphics2D) dummy.getGraphics();
        FontMetrics dummyFontMetrics = dummyGraphics.getFontMetrics(font);

        Rectangle2D textRectangle = dummyFontMetrics.getStringBounds(ALL_ASCII_CHARS, dummyGraphics);
        BufferedImage textImage = new BufferedImage((int) textRectangle.getWidth(), (int) textRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D textGraphics = (Graphics2D) textImage.getGraphics();
        int w = textImage.getWidth();
        int h = textImage.getWidth();

        textGraphics.setBackground(TRANSPARENT_COLOR);
        textGraphics.fillRect(0, 0, w, h);
        textGraphics.setColor(Color.WHITE);
        if (antialiasing) {
            textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        textGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ?
                RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        textGraphics.drawString(ALL_ASCII_CHARS, 4, font.getSize());

        TextureGL textureGL = TextureGL.newTexture(font.getFontName(), textImage, GL_TEXTURE_2D, true, false,null);

        PipelineShaderFontRendererProgramGL prog = PipelineShaderFontRendererProgramGL.create();
        prog.setupUniforms();

        VertexArrayObjectGL vao = VertexArrayObjectGL.newVertexArrayObject(null, () -> {
            glVertexAttribPointer(0, );
        });

        cachedFontRenderer = new FontRenderer(prog, textureGL, textGraphics.getFontMetrics(font));
        FONT_RENDERER_CACHE.put(font, cachedFontRenderer);
        return cachedFontRenderer;
    }

    public void setPvmMatrix(Matrix4f matrix) {
        this.pvmMatrix = matrix;
    }

    public void drawString(String string, int x, int y, int color) {
        for (char c : string.toCharArray()) {
            int charPos = ALL_ASCII_CHARS.indexOf(c);
            if (charPos == -1)
                continue;

            int lengthBeforeChar = fontMetrics.stringWidth(ALL_ASCII_CHARS.substring(charPos));
            int charLength = fontMetrics.charWidth(c);
            int charHeight = fontMetrics.getHeight();

            this.appendChar(x, y, (charPos == 0 ? 0 : lengthBeforeChar + 4), 0, charLength, charHeight, color);
        }

        this.draw();
    }

    protected void appendChar(int x, int y, int u, int v, int w, int h, int color) {
        if (charactersDrawnToScreen >= 6553)
            throw new RuntimeException("too many chars oops");
        charactersDrawnToScreen++;
        fontDataBuffer.putShort((short) x);
        fontDataBuffer.putShort((short) y);
        fontDataBuffer.put((byte) u);
        fontDataBuffer.put((byte) v);
        fontDataBuffer.put((byte) w);
        fontDataBuffer.put((byte) h);
        color = ((color >> 1) & 0x7F000000) | (color & 0xFFFFFF);
        fontDataBuffer.putInt(color);
    }

    protected void draw() {

    }
}
