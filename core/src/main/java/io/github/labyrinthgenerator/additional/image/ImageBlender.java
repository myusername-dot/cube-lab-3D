package io.github.labyrinthgenerator.additional.image;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class ImageBlender {

    public static BufferedImage pixmapToBufferedImage(Pixmap pixmap) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PixmapIO.PNG writer = new PixmapIO.PNG(pixmap.getWidth() * pixmap.getHeight() * 4);
            try {
                writer.setFlipY(false);
                writer.setCompression(Deflater.NO_COMPRESSION);
                writer.write(baos, pixmap);
            } finally {
                writer.dispose();
            }
            return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        }
    }

    public static BufferedImage imageToBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image; // If it's already a BufferedImage, no conversion needed
        }

        // Create a new BufferedImage with the same dimensions and type (e.g., ARGB)
        BufferedImage bufferedImage = new BufferedImage(
            image.getWidth(null),
            image.getHeight(null),
            BufferedImage.TYPE_INT_ARGB);

        // Draw the original Image onto the BufferedImage
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose(); // Release graphics resources

        return bufferedImage;
    }

    public static BufferedImage applyGaussianBlur(BufferedImage originalImage) throws IOException {
        float sigma = 1.0f;
        int kernelRadius = 5;
        int size = kernelRadius * 2 + 1;
        float[] data = new float[size * size];
        float normalization = 1.0f / (float) (Math.PI * 2 * sigma * sigma);
        float sum = 0.0f;

        for (int i = -kernelRadius; i <= kernelRadius; i++) {
            for (int j = -kernelRadius; j <= kernelRadius; j++) {
                float value = normalization * (float) Math.exp(-(i * i + j * j) / (2 * sigma * sigma));
                data[(i + kernelRadius) * size + (j + kernelRadius)] = value;
                sum += value;
            }
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage blurredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        op.filter(originalImage, blurredImage);

        return blurredImage;
    }

    public static Pixmap bufferedImageToPixmap(BufferedImage image, int maskARGB) throws IOException {
        Pixmap pixmap = new Pixmap(image.getWidth(), image.getHeight(), Pixmap.Format.RGBA8888);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int rgb = image.getRGB(i, j);
                // @formatter:off
                int a = (rgb >> 24) & (0xff & maskARGB >> 24);
                int r = (rgb >> 16) & (0xff & maskARGB >> 16);
                int g = (rgb >> 8 ) & (0xff & maskARGB >> 8);
                int b =  rgb        &  0xff & maskARGB;
                // @formatter:on
                pixmap.drawPixel(i, j, (r << 24) | (g << 16) | (b << 8) | a);
            }
        }
        return pixmap;
    }
}
