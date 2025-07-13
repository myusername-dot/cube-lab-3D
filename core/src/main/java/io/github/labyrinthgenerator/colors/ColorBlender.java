package io.github.labyrinthgenerator.colors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

public class ColorBlender {

    public static int create(int r, int g, int b, float a) {
        return ((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b & 0xff) << 8) | ((int) (a * 255) & 0xff);
    }

    // @formatter:off
    public static Quaternion extractRGBA(int color) {
        int r = (color >> 24) & 0xff;
        int g = (color >> 16) & 0xff;
        int b = (color >> 8 ) & 0xff;
        int a =  color        & 0xff;

        return new Quaternion(r, g, b, a);
    }

    public static Vector3i extractRGB(int color) {
        int r = (color >> 24) & 0xff;
        int g = (color >> 16) & 0xff;
        int b = (color >> 8 ) & 0xff;

        return new Vector3i(r, g, b);
    }
    // @formatter:on

    public static int nor(int pixel, float alpha) {
        Vector3i tmp = extractRGB(pixel);
        nor(tmp);
        return create(tmp.x, tmp.x, tmp.z, alpha);
    }

    private static void nor(Vector3i tmp) {
        Vector3 tmp1 = new Vector3(tmp.x, tmp.y, tmp.z).nor().scl(255);
        tmp.set((int) tmp1.x, (int) tmp1.y, (int) tmp1.z);
    }

    private static int max(Vector3i c) {
        return Math.max(Math.max(c.x, c.y), c.z);
    }

    private static int max(Quaternion c) {
        return (int) Math.max(Math.max(c.x, c.y), c.z);
    }

    private static int max(Quaternion c1, Quaternion c2) {
        return Math.max(max(c1), max(c2));
    }

    private static int max(Vector3i c1, Vector3i c2) {
        return Math.max(max(c1), max(c2));
    }

    private static int min(Vector3i c) {
        return Math.min(Math.min(c.x, c.y), c.z);
    }

    private static int min(Quaternion c) {
        return (int) Math.min(Math.min(c.x, c.y), c.z);
    }

    private static int min(Quaternion c1, Quaternion c2) {
        return Math.min(min(c1), min(c2));
    }

    // or multiple gray?
    private static int avgMaxMin(Vector3i c) {
        int max = max(c);
        int min = min(c);

        return min + (max - min) / 2;
    }

    private static int avg(Vector3i c) {
        return (c.x + c.y + c.z) / 3;
    }

    private static void clamp(Vector3i c) {
        c.x = Math.min(255, Math.max(0, c.x));
        c.y = Math.min(255, Math.max(0, c.y));
        c.z = Math.min(255, Math.max(0, c.z));
    }

    public static int clamp(int minSrc, int maxSrc, int dst) {
        Vector3i c1 = extractRGB(minSrc);
        Vector3i c2 = extractRGB(maxSrc);
        Quaternion c3 = extractRGBA(dst);

        int min1 = min(c1);
        int max2 = max(c2);

        int r = (int) (MathUtils.clamp(c3.x, min1, max2));
        int g = (int) (MathUtils.clamp(c3.y, min1, max2));
        int b = (int) (MathUtils.clamp(c3.z, min1, max2));

        return create(r, g, b, c3.w / 255f);
    }

    public static void reMax(Quaternion c1, Quaternion c2, Vector3i tmp) {
        int max1 = max(c1, c2);

        int max2 = max(tmp);
        float scl = (float) max1 / max2;
        tmp.scl(scl);
    }

    public static void reMax(Vector3i c1, Vector3i c2, Vector3i tmp) {
        int max1 = max(c1, c2);

        int max2 = max(tmp);
        float scl = (float) max1 / max2;
        tmp.scl(scl);
    }

    public static int subGray(int pixel, float factor, float alpha) {
        Vector3i c = extractRGB(pixel);

        int avg = avgMaxMin(c);

        int diff = Math.abs(avg - c.x) + Math.abs(avg - c.y) + Math.abs(avg - c.z);

        int gray = Math.max((int) (factor * 255f) - diff, 0);
        int grayC = create(gray, gray, gray, alpha);

        return sub(pixel, grayC, alpha);
    }

    public static int saturation(int pixel, float saturationFactor, float alpha) {
        Vector3i c1 = extractRGB(pixel);

        int gray = (int) (0.299 * c1.x + 0.587 * c1.y + 0.114 * c1.z);
        Vector3i grayColor = new Vector3i(gray, gray, gray);

        Vector3i newColor = new Vector3i(
            (int) (c1.x + (grayColor.x - c1.x) * saturationFactor),
            (int) (c1.y + (grayColor.y - c1.y) * saturationFactor),
            (int) (c1.z + (grayColor.z - c1.z) * saturationFactor)
        );

        clamp(newColor);

        return mix(pixel, create(newColor.x, newColor.y, newColor.z, alpha), saturationFactor, alpha);
    }

    public static int add(int pixel1, int pixel2, float alpha1, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int r = (int) Math.min(c1.x * alpha1 + c2.x, 255f);
        int g = (int) Math.min(c1.y * alpha1 + c2.y, 255f);
        int b = (int) Math.min(c1.z * alpha1 + c2.z, 255f);

        return create(r, g, b, alpha);
    }

    public static int addNor(int pixel1, int pixel2, float alpha1, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int r = (int) (c1.x * alpha1 + c2.x * (1f - alpha1));
        int g = (int) (c1.y * alpha1 + c2.y * (1f - alpha1));
        int b = (int) (c1.z * alpha1 + c2.z * (1f - alpha1));

        Vector3i tmp = new Vector3i(r, g, b);
        nor(tmp);
        reMax(c1, c2, tmp);

        return create(r, g, b, alpha);
    }

    public static int sub(int pixel1, int pixel2, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int r = Math.max(c1.x - c2.x, 0);
        int g = Math.max(c1.y - c2.y, 0);
        int b = Math.max(c1.z - c2.z, 0);

        return create(r, g, b, alpha);
    }

    public static int mix(int pixel1, int pixel2, float alpha1, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int r = (int) (c1.x * alpha1 + c2.x * (1f - alpha1));
        int g = (int) (c1.y * alpha1 + c2.y * (1f - alpha1));
        int b = (int) (c1.z * alpha1 + c2.z * (1f - alpha1));

        Vector3i tmp = new Vector3i(r, g, b);
        clamp(tmp);

        return create(tmp.x, tmp.y, tmp.z, alpha);
    }

    public static int replacementMin(int pixel1, int pixel2, float threshold, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int factorC = (int) (threshold * 255f);
        int avg = avg(c1);

        int r = avg < factorC ? Math.min(c1.x, c2.x) : c1.x;
        int g = avg < factorC ? Math.min(c1.y, c2.y) : c1.y;
        int b = avg < factorC ? Math.min(c1.z, c2.z) : c1.z;

        return create(r, g, b, alpha);
    }

    public static int multiply(int pixel1, int pixel2, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        int r = (int) (c1.x * c2.x / 255.0f);
        int g = (int) (c1.y * c2.y / 255.0f);
        int b = (int) (c1.z * c2.z / 255.0f);

        return create(r, g, b, alpha);
    }

    public static int screen(int pixel1, int pixel2, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        // Экранный режим
        int r = 255 - (int) ((255.0f - c1.x) * (255.0f - c2.x)) / 255;
        int g = 255 - (int) ((255.0f - c1.y) * (255.0f - c2.y)) / 255;
        int b = 255 - (int) ((255.0f - c1.z) * (255.0f - c2.z)) / 255;

        return create(r, g, b, alpha);
    }

    public static int max(int pixel1, int pixel2, float alpha1, float alpha) {
        Vector3i c1 = extractRGB(pixel1);
        Vector3i c2 = extractRGB(pixel2);

        // замена светлым
        int r = (int) (Math.max(c1.x * alpha1, c2.x));
        int g = (int) (Math.max(c1.y * alpha1, c2.y));
        int b = (int) (Math.max(c1.z * alpha1, c2.z));

        return create(r, g, b, alpha);
    }

    public static int setAlpha(int pixel1, float alpha) {
        return pixel1 | (int) (alpha * 255f);
    }
}
