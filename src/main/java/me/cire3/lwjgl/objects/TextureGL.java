package me.cire3.lwjgl.objects;

import me.cire3.App;
import me.cire3.lwjgl.ObjectGL;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;

public class TextureGL extends ObjectGL {
    private static final Map<String, TextureGL> STRING_TEXTURE_GL_MAP = new HashMap<>();
    private static final Map<byte[], TextureGL> BYTE_ARRAY_TEXTURE_GL_MAP = new HashMap<>();
    private int texture;
    private byte[] data;
    private String name;
    private int textureType;

    /**
     * Automatically puts this TextureGL into the cache
     * */
    private TextureGL(int texture, byte[] data, String name, int textureType) {
        this.texture = texture;
        this.data = data;
        this.name = name;
        this.textureType = textureType;

        if (!BYTE_ARRAY_TEXTURE_GL_MAP.containsKey(data))
            BYTE_ARRAY_TEXTURE_GL_MAP.put(data, this);

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

    public static TextureGL newTexture(String texture) {
        return newTexture(texture, GL_TEXTURE_2D, true);
    }

    /**
     * @return a TextureGL instance that wraps the underlying texture. You must set the texParameter yourself
     * */
    public static TextureGL newTexture(String texture, int textureType, boolean rgba) {
        if (STRING_TEXTURE_GL_MAP.containsKey(texture)) {
            TextureGL textureGL =  STRING_TEXTURE_GL_MAP.get(texture);
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

            if (BYTE_ARRAY_TEXTURE_GL_MAP.containsKey(image))
                return BYTE_ARRAY_TEXTURE_GL_MAP.get(image);

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
            bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());

            ByteBuffer buf = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * (rgba ? 4 : 3));

            for(int y = 0; y < bufferedImage.getHeight(); y++){
                for(int x = 0; x < bufferedImage.getWidth(); x++){
                    int pixel = pixels[y * bufferedImage.getWidth() + x];
                    buf.put((byte) ((pixel >> 16) & 0xFF));
                    buf.put((byte) ((pixel >> 8) & 0xFF));
                    buf.put((byte) (pixel & 0xFF));
                    buf.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buf.flip();

            int id = glGenTextures();
            glBindTexture(textureType, id);
            glTexImage2D(GL_TEXTURE_2D, 0, (rgba ? GL_RGBA8 : GL_RGB8), bufferedImage.getWidth(), bufferedImage.getHeight(),
                    0, (rgba ? GL_RGBA : GL_RGB), GL_UNSIGNED_BYTE, buf);

            return new TextureGL(id, image, texture, textureType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
