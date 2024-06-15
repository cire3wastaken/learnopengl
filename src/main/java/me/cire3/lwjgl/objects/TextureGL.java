package me.cire3.lwjgl.objects;

import me.cire3.App;
import me.cire3.lwjgl.ObjectGL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11C.*;

public class TextureGL extends ObjectGL {
    private int texture;

    public TextureGL(int texture) {
        this.texture = texture;
    }

    @Override
    public void cleanup() {
        if (texture != -1) {
            glDeleteTextures(texture);
            texture = -1;
        }
    }

    public static TextureGL newTexture(String texture) {
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

            int[] pixels
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
