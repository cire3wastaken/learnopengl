package me.cire3;

import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderFontRendererProgramGL;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

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
    // we need raw access so we cant use VertexBufferObjectGL
    private int instancesDataBufferId;
    private Matrix4f pvmMatrix;
    private final FloatBuffer pvmMatrixBuffer;
    private int pvmMatrixSerial = Integer.MIN_VALUE;
    private int renderedPvmMatrixSerial = Integer.MIN_VALUE;
    private TextureGL internalFontTextureAtlas;

    private ByteBuffer fontDataBuffer;
    private int charactersDrawnToScreen;

    private final int asciiCharsLength;
    private FontMetrics fontMetrics;

    public FontRenderer(Font font, boolean antialiasing, boolean fractional) {
        // -------------------- SETUP --------------------
        this.pvmMatrix = new Matrix4f();
        this.fontDataBuffer = BufferUtils.createByteBuffer(65536);
        this.pvmMatrixBuffer = BufferUtils.createFloatBuffer(16);

        BufferedImage textImage = getBufferedImageForFont(font);

        Graphics2D textGraphics = (Graphics2D) textImage.getGraphics();
        int w = textImage.getWidth();
        int h = textImage.getWidth();

        // -------------------- GENERATE FONT TEXTURES --------------------
        textGraphics.setBackground(TRANSPARENT_COLOR);
        textGraphics.fillRect(0, 0, w, h);
        textGraphics.setColor(Color.WHITE);
        if (antialiasing) {
            textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        textGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractional ?
                RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        textGraphics.drawString(ALL_ASCII_CHARS, 4, font.getSize());

        // -------------------- SAVE FONT TEXTURES --------------------
        this.internalFontTextureAtlas = TextureGL.newTexture(font.getFontName(), textImage, GL_TEXTURE_2D, true, false,null);
        this.fontMetrics = textGraphics.getFontMetrics(font);
        this.asciiCharsLength = this.fontMetrics.stringWidth(ALL_ASCII_CHARS);

        // -------------------- SETUP OPENGL --------------------
        this.prog = PipelineShaderFontRendererProgramGL.create();
        this.prog.setupUniforms();
        glUniform1i(this.prog.getUniforms().u_texture.getId(), 0);

        this.vao = VertexArrayObjectGL.newVertexArrayObject(null);

        // -------------------- DEFAULT VERTICES (BOILERPLATE) --------------------
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glVertexAttribDivisor(0, 0);
        glEnableVertexAttribArray(0);


        // -------------------- DATA TO DRAW --------------------
        this.instancesDataBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instancesDataBufferId);
        glBufferData(GL_ARRAY_BUFFER, fontDataBuffer.remaining(), GL_STREAM_DRAW);

        // -------------------- COLOR --------------------
        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, false, 16, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);

        // -------------------- TEXTURE POS --------------------
        glVertexAttribPointer(2, 2, GL_INT, false, 16, 4 * Byte.BYTES);
        glVertexAttribDivisor(2, 1);
        glEnableVertexAttribArray(2);

        // -------------------- CHAR POS --------------------
        glVertexAttribPointer(3, 2, GL_SHORT, false, 16, 4 * Byte.BYTES + 2 * Integer.BYTES);
        glVertexAttribDivisor(3, 1);
        glEnableVertexAttribArray(3);
    }

    private static @NotNull BufferedImage getBufferedImageForFont(Font font) {
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dummyGraphics = (Graphics2D) dummy.getGraphics();
        FontMetrics dummyFontMetrics = dummyGraphics.getFontMetrics(font);

        Rectangle2D textRectangle = dummyFontMetrics.getStringBounds(ALL_ASCII_CHARS, dummyGraphics);
        return new BufferedImage((int) textRectangle.getWidth(), (int) textRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    public static FontRenderer newFontRenderer(Font font, boolean antialiasing, boolean fractionalMetrics) {
        FontRenderer cachedFontRenderer = FONT_RENDERER_CACHE.get(font);
        if (cachedFontRenderer != null)
            return cachedFontRenderer;

        cachedFontRenderer = new FontRenderer(font, antialiasing, fractionalMetrics);
        FONT_RENDERER_CACHE.put(font, cachedFontRenderer);
        return cachedFontRenderer;
    }

    public void setPvmMatrix(Matrix4f matrix) {
        if (pvmMatrix.equals(matrix))
            return;

        this.pvmMatrix = matrix;
        this.pvmMatrixSerial++;
    }

    public void drawString(String string, int x, int y, int color) {
        this.reset();

        for (char c : string.toCharArray()) {
            int charPos = ALL_ASCII_CHARS.indexOf(c);
            if (charPos == -1)
                continue;

            int lengthBeforeChar = fontMetrics.stringWidth(ALL_ASCII_CHARS.substring(charPos));

            this.appendChar(x, y, (charPos == 0 ? 0 : lengthBeforeChar + 4), 0, color);
        }

        this.draw();
    }

    protected void appendChar(int x, int y, int u, int v, int color) {
        if (charactersDrawnToScreen >= 4096)
            throw new RuntimeException("too many chars oops");
        charactersDrawnToScreen++;
        color = ((color >> 1) & 0x7F000000) | (color & 0xFFFFFF);
        fontDataBuffer.putInt(color);       // 4 bytes
        fontDataBuffer.putInt(u);           // 4 bytes
        fontDataBuffer.putInt(v);           // 4 bytes
        fontDataBuffer.putShort((short) x); // 2 bytes
        fontDataBuffer.putShort((short) y); // 2 bytes
        // total is 16 bytes
    }

    protected void reset() {
        this.fontDataBuffer.clear();
        this.charactersDrawnToScreen = 0;
    }

    protected void draw() {
        if (renderedPvmMatrixSerial != pvmMatrixSerial) {
            renderedPvmMatrixSerial = pvmMatrixSerial;
            glUniformMatrix4fv(prog.getUniforms().u_pvmMatrix.getId(), false, pvmMatrix.get(pvmMatrixBuffer));
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(internalFontTextureAtlas.getTextureType(), internalFontTextureAtlas.getTextureId());

        glBindBuffer(GL_ARRAY_BUFFER, instancesDataBufferId);
        vao.bind();

        if (charactersDrawnToScreen > 0) {
            int pos = fontDataBuffer.position();
            int limit = fontDataBuffer.limit();
            fontDataBuffer.flip();

            glBufferSubData(GL_ARRAY_BUFFER, 0, fontDataBuffer);

            fontDataBuffer.position(pos);
            fontDataBuffer.limit(limit);

            glDrawArraysInstanced(GL_TRIANGLES, 0, 6, charactersDrawnToScreen);
        }
    }
}
