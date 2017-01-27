package xyz.izaak.radon;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * A class containing static utility methods for performing file IO. File paths are relative to the Java project's
 * <code>/resource</code> directory.
 */
public class Resource {

    public static BufferedImage imageFromFile(String filepath, boolean flip) throws IOException {
        InputStream inputStream = Resource.class.getClassLoader().getResourceAsStream(filepath);
        BufferedImage image = ImageIO.read(inputStream);

        if (flip) {
            AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
            transform.translate(0, -image.getHeight());
            AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = operation.filter(image, null);
        }

        return image;
    }

    public static BufferedImage imageFromFile(String filepath) throws IOException {
        return imageFromFile(filepath, true);
    }

    public static String stringFromFile(String filepath) {
        Scanner scanner = scannerForFile(filepath, "\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }

    public static String stringFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }

    public static Scanner scannerForFile(String filepath) {
        return scannerForFile(filepath, null);
    }

    public static Scanner scannerForFile(String filepath, String delimiter) {
        InputStream inputStream = streamFromFile(filepath);
        Scanner scanner = new Scanner(inputStream);
        if(delimiter != null) scanner.useDelimiter(delimiter);
        return scanner;
    }

    public static InputStream streamFromFile(String filepath) {
        return Resource.class.getClassLoader().getResourceAsStream(filepath);
    }
}
