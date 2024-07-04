package me.cire3.lwjgl.objects;

import me.cire3.App;
import me.cire3.lwjgl.ObjectGL;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureGL extends ObjectGL {
    private static final Map<String, TextureGL> STRING_TEXTURE_GL_MAP = new HashMap<>();
    private int texture;
    private String name;
    private int textureType;

    /**
     * Automatically puts this TextureGL into the cache
     */
    private TextureGL(int texture, String name, int textureType) {
        this.texture = texture;
        this.name = name;
        this.textureType = textureType;

        if (!STRING_TEXTURE_GL_MAP.containsKey(name))
            STRING_TEXTURE_GL_MAP.put(name, this);
    }

    @Override
    public void cleanup() {
        if (texture != -1) {
            glDeleteTextures(texture);
            texture = -1;
        }
    }

    public int getTextureId() {
        return texture;
    }

    public String getName() {
        return name;
    }

    public int getTextureType() {
        return textureType;
    }

    public static TextureGL newTexture(String texture, int textureType, boolean isRgba) {
        return newTexture(texture, textureType, isRgba, TextureParameterConfigurer.DEFAULT_CONFIGURER);
    }

    public static TextureGL newTexture(String texture, int textureType, boolean isRgba, TextureParameterConfigurer configurer) {
        return newTexture(texture, textureType, true, isRgba, configurer);
    }

    /**
     * @return a TextureGL instance that wraps the underlying texture.
     * @apiNote You must set the texParameter yourself via passing a texture parameter configurer via the last param
     */
    public static TextureGL newTexture(String texture, int textureType, boolean flipTexture, boolean isRgba, TextureParameterConfigurer configurer) {
        if (STRING_TEXTURE_GL_MAP.containsKey(texture)) {
            TextureGL textureGL = STRING_TEXTURE_GL_MAP.get(texture);
            if (textureGL.textureType == textureType)
                return textureGL;
        }

        try (ByteArrayOutputStream bao = new ByteArrayOutputStream();
             InputStream is = App.getInputStream("textures/" + texture)) {

            int read = is.read();
            while (read != -1) {
                bao.write(read);
                read = is.read();
            }

            byte[] image = bao.toByteArray();
            bao.close();

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            return newTexture(texture, bufferedImage, textureType, isRgba, flipTexture, configurer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TextureGL newTexture(String name, BufferedImage bufferedImage, int textureType, boolean isRgba, boolean flipTexture, TextureParameterConfigurer configurer) {
        return new TextureGL(processImage(bufferedImage, textureType, isRgba, flipTexture, configurer), name, textureType);
    }

    private static int processImage(BufferedImage image, int textureType, boolean isRgba, boolean flipTexture, TextureParameterConfigurer configurer) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        // probably too big to use MemoryStack
        ByteBuffer buf = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * (isRgba ? 4 : 3));

        // yes
        for (int y = flipTexture ? image.getHeight() - 1 : 0; flipTexture ? y >= 0 : y < image.getHeight(); y += flipTexture ? -1 : 1) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buf.put((byte) ((pixel >> 16) & 0xFF));
                buf.put((byte) ((pixel >> 8) & 0xFF));
                buf.put((byte) (pixel & 0xFF));
                if (isRgba)
                    buf.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buf.flip();

        int id = glGenTextures();
        glBindTexture(textureType, id);
        glTexImage2D(GL_TEXTURE_2D, 0, (isRgba ? GL_RGBA8 : GL_RGB8), image.getWidth(), image.getHeight(),
                0, (isRgba ? GL_RGBA : GL_RGB), GL_UNSIGNED_BYTE, buf);

        Objects.requireNonNullElse(configurer, TextureParameterConfigurer.DEFAULT_CONFIGURER).setup();

        glGenerateMipmap(GL_TEXTURE_2D);
        MemoryUtil.memFree(buf);

        return id;
    }

    @FunctionalInterface
    public interface TextureParameterConfigurer {
        TextureParameterConfigurer DEFAULT_CONFIGURER = () -> {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        };

        void setup();
    }
}
