package me.cire3;

import me.cire3.lwjgl.objects.TextureGL;
import me.cire3.lwjgl.objects.VertexArrayObjectGL;
import me.cire3.lwjgl.objects.VertexBufferObjectGL;
import me.cire3.lwjgl.objects.programs.PipelineShaderFontRendererProgramGL;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class FontRenderer {
    private static final Map<Font, FontRenderer> FONT_RENDERER_CACHE = new HashMap<>();
    public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    public static final String ALL_ASCII_CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    private static final int BYTES_PER_CHARACTER = 10;
    private static final int CHARACTER_LIMIT = 6553;

    private final PipelineShaderFontRendererProgramGL shaderProgram;
    private final FloatBuffer matrixCopyBuffer;
    private final VertexArrayObjectGL vertexArray;
    // need raw control over stuff
    private int instancesBufferId = -1;

    private float stateColorR = -999.0f;
    private float stateColorG = -999.0f;
    private float stateColorB = -999.0f;
    private float stateColorA = -999.0f;
    private int stateColorSerial = -1;

    private float stateColorBiasR = -999.0f;
    private float stateColorBiasG = -999.0f;
    private float stateColorBiasB = -999.0f;
    private float stateColorBiasA = -999.0f;

    private final Matrix4f tmpMatrix = new Matrix4f();
    private final Vector4f tmpVector = new Vector4f();
    private Matrix4f pvmMatrix = new Matrix4f();
    private int pvmMatrixSerial = Integer.MIN_VALUE;
    private int renderedPvmMatrixSerial = Integer.MIN_VALUE;

    private float charWidthValue = -1;
    private float charHeightValue = -1;
    private float charCoordWidthValue = -1.0f;
    private float charCoordHeightValue = -1.0f;

    private TextureGL fontTexture;
    private FontMetrics fontMetrics;

    private FontRenderer(Font font, boolean antialias, boolean fractionalMetrics) {
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dummyGraphics = (Graphics2D) dummy.getGraphics();
        FontMetrics dummyFontMetrics = dummyGraphics.getFontMetrics(font);

        Rectangle2D textRectangle = dummyFontMetrics.getStringBounds(ALL_ASCII_CHARS, dummyGraphics);
        BufferedImage textImage = new BufferedImage((int) textRectangle.getWidth(), (int) textRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D textGraphics = (Graphics2D) textImage.getGraphics();
        int w = textImage.getWidth();
        int h = textImage.getWidth();

        // -------------------- GENERATE FONT TEXTURES --------------------
        textGraphics.setBackground(TRANSPARENT_COLOR);
        textGraphics.fillRect(0, 0, w, h);
        if (antialias) {
            textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        textGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ?
                RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        textGraphics.setColor(Color.BLACK);
        textGraphics.drawString(ALL_ASCII_CHARS, 0, 0);

        // -------------------- SAVE FONT TEXTURES --------------------
        this.fontTexture = TextureGL.newTexture(font.getFontName(), textImage, GL_TEXTURE_2D, true, false,null);
        this.fontMetrics = textGraphics.getFontMetrics(font);

        // -------------------- OPENGL STUFF--------------------
        this.shaderProgram = PipelineShaderFontRendererProgramGL.create();

        matrixCopyBuffer = BufferUtils.createFloatBuffer(16);
        fontDataBuffer = BufferUtils.createByteBuffer(CHARACTER_LIMIT * BYTES_PER_CHARACTER);
        fontBoldDataBuffer = BufferUtils.createByteBuffer(CHARACTER_LIMIT * BYTES_PER_CHARACTER);

        shaderProgram.setupUniforms();

        vertexArray = VertexArrayObjectGL.newVertexArrayObject(null);
        VertexBufferObjectGL vertexBuffer = VertexBufferObjectGL.newVertexBufferObjectGL(null);
        instancesBufferId = glGenBuffers();

        FloatBuffer verts = MemoryUtil.memAllocFloat(108);
        verts.put(new float[] {

                // (0 - 6 - 12) regular:

                0.0f, 0.0f, 0.25f,  0.0f, 1.0f, 0.25f,  1.0f, 0.0f, 0.25f,
                1.0f, 0.0f, 0.25f,  0.0f, 1.0f, 0.25f,  1.0f, 1.0f, 0.25f,
                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 1.0f, 0.0f,

                // (12 - 24 - 36) bold shadow:

                0.0f, 0.0f, 0.25f,  0.0f, 1.0f, 0.25f,  1.0f, 0.0f, 0.25f,
                1.0f, 0.0f, 0.25f,  0.0f, 1.0f, 0.25f,  1.0f, 1.0f, 0.25f,
                0.0f, 0.0f, 0.75f,  0.0f, 1.0f, 0.75f,  1.0f, 0.0f, 0.75f,
                1.0f, 0.0f, 0.75f,  0.0f, 1.0f, 0.75f,  1.0f, 1.0f, 0.75f,

                0.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,  0.0f, 1.0f, 0.0f,  1.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.5f,  0.0f, 1.0f, 0.5f,  1.0f, 0.0f, 0.5f,
                1.0f, 0.0f, 0.5f,  0.0f, 1.0f, 0.5f,  1.0f, 1.0f, 0.5f

        });
        verts.flip();

        vertexArray.bind();
        vertexBuffer.bind();
        vertexBuffer.update(verts);

        MemoryUtil.memFree(verts);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glVertexAttribDivisor(0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, instancesBufferId);
        glBufferData(GL_ARRAY_BUFFER, fontDataBuffer.remaining(), GL_STREAM_DRAW);

        glVertexAttribPointer(1, 2, GL_SHORT, false, 10, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_UNSIGNED_BYTE, false, 10, 4);
        glVertexAttribDivisor(2, 1);
        glEnableVertexAttribArray(2);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_UNSIGNED_BYTE, true, 10, 6);
        glVertexAttribDivisor(3, 1);
        glEnableVertexAttribArray(4);
    }

    public static FontRenderer newFontRenderer(Font font, boolean antialias, boolean fractionalMetrics) {
        FontRenderer cachedFontRenderer = FONT_RENDERER_CACHE.get(font);
        if (cachedFontRenderer != null)
            return cachedFontRenderer;
        cachedFontRenderer = new FontRenderer(font, antialias, fractionalMetrics);
        FONT_RENDERER_CACHE.put(font, cachedFontRenderer);
        return cachedFontRenderer;
    }

    public void renderStringAtPos0(String string) {
        glBindTexture(fontTexture.getTextureType(), fontTexture.getTextureId());
        this.begin();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        boolean hasStrike = false;

        for (int i = 0; i < string.length(); ++i) {
            char c0 = string.charAt(i);
            if (c0 == 167 && i + 1 < string.length()) {
                int i1 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(string.charAt(i + 1)));
                if (i1 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (i1 < 0 || i1 > 15) {
                        i1 = 15;
                    }
                    int j1 = this.colorCode[i1];
                    this.textColor = j1 | (this.textColor & 0xFF000000);
                } else if (i1 == 16) {
                    this.randomStyle = true;
                } else if (i1 == 17) {
                    this.boldStyle = true;
                } else if (i1 == 18) {
                    this.strikethroughStyle = true;
                } else if (i1 == 19) {
                    this.underlineStyle = true;
                } else if (i1 == 20) {
                    this.italicStyle = true;
                } else if (i1 == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    this.textColor = ((int) (this.alpha * 255.0f) << 24) | ((int) (this.red * 255.0f) << 16)
                            | ((int) (this.green * 255.0f) << 8) | (int) (this.blue * 255.0f);
                }

                ++i;
            } else {
                int j = temporaryCodepointArray[i];

                if (this.randomStyle && j != -1) {
                    int k = this.getCharWidth(c0);
                    String chars = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

                    char c1;
                    while (true) {
                        j = this.fontRandom.nextInt(chars.length());
                        c1 = chars.charAt(j);
                        if (k == this.getCharWidth(c1)) {
                            break;
                        }
                    }

                    c0 = c1;
                }

                float f = this.appendCharToBuffer(j, this.textColor, this.boldStyle, this.italicStyle);

                if (this.strikethroughStyle) {
                    hasStrike = true;
                    worldrenderer.pos((double) this.posX, (double) (this.posY + (float) (this.FONT_HEIGHT / 2)), 0.0D)
                            .endVertex();
                    worldrenderer
                            .pos((double) (this.posX + f), (double) (this.posY + (float) (this.FONT_HEIGHT / 2)), 0.0D)
                            .endVertex();
                    worldrenderer.pos((double) (this.posX + f),
                            (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
                    worldrenderer
                            .pos((double) this.posX, (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D)
                            .endVertex();
                    worldrenderer.putColor4(this.textColor);
                }

                if (this.underlineStyle) {
                    hasStrike = true;
                    int l = this.underlineStyle ? -1 : 0;
                    worldrenderer.pos((double) (this.posX + (float) l),
                            (double) (this.posY + (float) this.FONT_HEIGHT), 0.0D).endVertex();
                    worldrenderer.pos((double) (this.posX + f), (double) (this.posY + (float) this.FONT_HEIGHT), 0.0D)
                            .endVertex();
                    worldrenderer
                            .pos((double) (this.posX + f), (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D)
                            .endVertex();
                    worldrenderer.pos((double) (this.posX + (float) l),
                            (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
                    worldrenderer.putColor4(this.textColor);
                }

                this.posX += (float) ((int) f);
            }
        }

        float texScale = 0.0625f;

        if(!hasStrike) {
            worldrenderer.finishDrawing();
        }

        if(parFlag) {
            if(hasStrike) {
                GlStateManager.color(0.25f, 0.25f, 0.25f, 1.0f);
                GlStateManager.translate(1.0f, 1.0f, 0.0f);
                tessellator.draw();
                GlStateManager.translate(-1.0f, -1.0f, 0.0f);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                InstancedFontRenderer.render(8, 8, texScale, texScale, true);
                EaglercraftGPU.renderAgain();
            }else {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                InstancedFontRenderer.render(8, 8, texScale, texScale, true);
            }
        }else {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            if(hasStrike) {
                tessellator.draw();
            }
            this.render(8, 8, texScale, texScale, false);
        }

        if(parFlag) {
            this.posX += 1.0f;
        }
    }

    private float appendCharToBuffer(int parInt1, int color, boolean boldStyle, boolean italicStyle) {
        if (parInt1 == 32) {
            return 4.0f;
        }else {
            int i = parInt1 % 16;
            int j = parInt1 / 16;
            float w = this.charWidth[parInt1];
            if(boldStyle) {
                this.appendBoldQuad((int)this.posX, (int)this.posY, i, j, color, italicStyle);
                ++w;
            }else {
                this.appendQuad((int)this.posX, (int)this.posY, i, j, color, italicStyle);
            }
            return w;
        }
    }

    private ByteBuffer fontDataBuffer = null;
    private int charactersDrawn = 0;
    private ByteBuffer fontBoldDataBuffer = null;
    private int boldCharactersDrawn = 0;
    private boolean hasOverflowed = false;
    private boolean hasBoldOverflowed = false;

    public void begin() {
        fontDataBuffer.clear();
        charactersDrawn = 0;
        fontBoldDataBuffer.clear();
        boldCharactersDrawn = 0;
        hasOverflowed = false;
        hasBoldOverflowed = false;
    }

    public void updateMatrix(Matrix4f matrix) {
        if (pvmMatrix.equals(matrix))
            return;
        pvmMatrix = matrix;
        pvmMatrixSerial++;
    }

    public void render(float charWidth, float charHeight, float charCoordWidth, float charCoordHeight, boolean shadow) {
        if(charactersDrawn == 0 && boldCharactersDrawn == 0) {
            return;
        }
        shaderProgram.bind();

        if(charWidth != charWidthValue || charHeight != charHeightValue) {
            charWidthValue = charWidth;
            charHeightValue = charHeight;
            glUniform2f(shaderProgram.getUniforms().u_charSize2f.getId(), (float)charWidth, (float)charHeight);
        }

        if(charCoordWidth != charCoordWidthValue || charCoordHeight != charCoordHeightValue) {
            charCoordWidthValue = charCoordWidth;
            charCoordHeightValue = charCoordHeight;
            glUniform2f(shaderProgram.getUniforms().u_charCoordSize2f.getId(), charCoordWidth, charCoordHeight);
        }

        if (pvmMatrixSerial != renderedPvmMatrixSerial) {
            renderedPvmMatrixSerial = pvmMatrixSerial;

            matrixCopyBuffer.clear();
            pvmMatrix.get(matrixCopyBuffer);
            matrixCopyBuffer.flip();

            glUniformMatrix4fv(shaderProgram.getUniforms().u_matrixTransform.getId(), false, matrixCopyBuffer);
        }

        glBindBuffer(GL_ARRAY_BUFFER, instancesBufferId);
        vertexArray.bind();

        if(charactersDrawn > 0) {
            int p = fontDataBuffer.position();
            int l = fontDataBuffer.limit();

            fontDataBuffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, fontDataBuffer);

            fontDataBuffer.position(p);
            fontDataBuffer.limit(l);

            glDrawArraysInstanced(GL_TRIANGLES, shadow ? 0 : 6, shadow ? 12 : 6, charactersDrawn);
        }

        if(boldCharactersDrawn > 0) {
            int p = fontBoldDataBuffer.position();
            int l = fontBoldDataBuffer.limit();

            fontBoldDataBuffer.flip();
            glBufferSubData(GL_ARRAY_BUFFER, 0, fontBoldDataBuffer);

            fontBoldDataBuffer.position(p);
            fontBoldDataBuffer.limit(l);

            glDrawArraysInstanced(GL_TRIANGLES, shadow ? 12 : 24, shadow ? 24 : 12, boldCharactersDrawn);
        }
    }

    public void appendQuad(int x, int y, int cx, int cy, int color, boolean italic) {
        if(hasOverflowed) {
            return;
        }
        if(charactersDrawn >= CHARACTER_LIMIT) {
            hasOverflowed = true;
            System.err.println("Font renderer buffer has overflowed! Exceeded {} regular characters, no more regular characters will be rendered."
                    .replace("{}", CHARACTER_LIMIT + ""));
            return;
        }
        ++charactersDrawn;
        ByteBuffer buf = fontDataBuffer;
        buf.putShort((short)x);
        buf.putShort((short)y);
        buf.put((byte)cx);
        buf.put((byte)cy);
        color = ((color >> 1) & 0x7F000000) | (color & 0xFFFFFF);
        if(italic) {
            color |= 0x80000000;
        }
        buf.putInt(color);
    }

    public void appendBoldQuad(int x, int y, int cx, int cy, int color, boolean italic) {
        if(hasBoldOverflowed) {
            return;
        }
        if(boldCharactersDrawn >= CHARACTER_LIMIT) {
            hasBoldOverflowed = true;
            System.err.println("Font renderer buffer has overflowed! Exceeded {} bold characters, no more bold characters will be rendered."
                    .replace("{}", CHARACTER_LIMIT + ""));
            return;
        }
        ++boldCharactersDrawn;
        ByteBuffer buf = fontBoldDataBuffer;
        buf.putShort((short)x);
        buf.putShort((short)y);
        buf.put((byte)cx);
        buf.put((byte)cy);
        color = ((color >> 1) & 0x7F000000) | (color & 0xFFFFFF);
        if(italic) {
            color |= 0x80000000;
        }
        buf.putInt(color);
    }
}