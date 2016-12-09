package xyz.izaak.radon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * A class containing static utility methods for performing file IO. File paths are relative to the Java project's
 * <code>/resource</code> directory.
 */
public class Resource {

    /**
     * Read the entire contents of a file into a String.
     *
     * @param filepath the path, relative to <code>/resources</code> of the file to be read
     * @return a String containing the file's entire contents
     */
    public static String stringFromFile(String filepath) {
        Scanner scanner = scannerForFile(filepath, "\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }

    /**
     * Read the entire contents of a file into a String.
     *
     * @param file a File object to be read
     * @return a String containing the file's entire contents
     * @throws FileNotFoundException if the file cannot be found
     */
    public static String stringFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }

    /**
     * Constructs a Scanner for a file with the default delimiter
     *
     * @param filepath the path of the file to create a Scanner for
     * @return a Scanner object for the file
     */
    public static Scanner scannerForFile(String filepath) {
        return scannerForFile(filepath, null);
    }

    /**
     * Constructs a Scanner for a file with a given delimiter
     * @param filepath the path of the file to create a Scanner for
     * @param delimiter the delimiter for the Scanner to use
     * @return a Scanner object for that file
     */
    public static Scanner scannerForFile(String filepath, String delimiter) {
        InputStream inputStream = Resource.class.getClassLoader().getResourceAsStream(filepath);
        Scanner scanner = new Scanner(inputStream);
        if(delimiter != null) scanner.useDelimiter(delimiter);
        return scanner;
    }
}
