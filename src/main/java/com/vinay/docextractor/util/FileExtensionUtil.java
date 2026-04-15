package com.vinay.docextractor.util;

import java.io.File;

/**
 * Utility methods for working with file extensions and paths.
 */
public class FileExtensionUtil {

    private FileExtensionUtil() {
        // Utility class
    }

    /**
     * Extracts the file extension from a file path (without the dot).
     * Returns an empty string if the file has no extension.
     *
     * @param filePath the full file path or file name
     * @return lowercase extension, e.g. "pdf", "docx", or ""
     */
    public static String getExtension(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Returns just the file name (without directory path).
     */
    public static String getFileName(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }
        return new File(filePath).getName();
    }

    /**
     * Returns the file name without its extension.
     */
    public static String getBaseName(String filePath) {
        String fileName = getFileName(filePath);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    /**
     * Returns true if the given path points to a readable file.
     */
    public static boolean isReadableFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.canRead();
    }
}
