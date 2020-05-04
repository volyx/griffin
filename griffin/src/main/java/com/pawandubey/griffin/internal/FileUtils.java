package com.pawandubey.griffin.internal;

public class FileUtils {

    /**
     * Returns a representation of the file path with an alternate extension.  If the file path has no extension,
     * then the provided extension is simply concatenated.  If the file path has an extension, the extension is
     * stripped and replaced with the provided extension.
     *
     * e.g. with a provided extension of ".bar"
     * foo -> foo.bar
     * foo.baz -> foo.bar
     *
     * @param filePath the file path to transform
     * @param extension the extension to use in the transformed path
     * @return the transformed path
     */
    public static String withExtension(String filePath, String extension) {
        if (filePath.toLowerCase().endsWith(extension)) {
            return filePath;
        }
        return removeExtension(filePath) + extension;
    }

    /**
     * Removes the extension (if any) from the file path.  If the file path has no extension, then it returns the same string.
     *
     * @return the file path without an extension
     */
    public static String removeExtension(String filePath) {
        int fileNameStart = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        int extensionPos = filePath.lastIndexOf('.');

        if (extensionPos > fileNameStart) {
            return filePath.substring(0, extensionPos);
        }
        return filePath;
    }
}
