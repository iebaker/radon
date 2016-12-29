package xyz.izaak.radon.mesh.texture;

import org.lwjgl.BufferUtils;
import xyz.izaak.radon.Resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_RGB8;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;

/**
 * Created by ibaker on 28/12/2016.
 */
public class Texture {
    private int texture;

    public Texture(String path) throws IOException {
        BufferedImage image = Resource.imageFromFile(path);
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        loadToRgbaBuffer(buffer, width, height, pixels);
        writeToGlTexture(buffer, width, height);
    }

    public int getTexture() {
        return texture;
    }

    private void loadToRgbaBuffer(ByteBuffer buffer, int width, int height, int[] pixels) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >>  8) & 0xFF)); // Green
                buffer.put((byte) ((pixel      ) & 0xFF)); // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();
    }

    private void writeToGlTexture(ByteBuffer buffer, int width, int height) {
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }
}
