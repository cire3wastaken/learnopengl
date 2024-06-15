package me.cire3.lwjgl.objects;

import me.cire3.App;
import me.cire3.lwjgl.ObjectGL;

import java.io.InputStream;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;

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

    // TODO
    public static TextureGL newTexture(String texture) {
        InputStream is = App.getInputStream("textures/" + texture);
        PNGDecoder decoder = new PNGDecoder(is);
    }
}
