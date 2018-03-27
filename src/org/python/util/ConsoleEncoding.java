package org.python.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnr.posix.util.Platform;

public final class ConsoleEncoding {

    private static final Pattern DIGITS_PATTERN = Pattern.compile("[1-9][0-9]+");

    /**
     * Returns the actual console encoding
     * <p>
     * On Windows, this is determined with the command {@code chcp}, for all other platforms it is the system property {@code file.encoding}.
     * 
     * @return the console encoding
     */
    public static String get() {
        if (Platform.IS_WINDOWS) {
            return getWindowsConsoleEncoding();
        } else {
            return System.getProperty("file.encoding");
        }
    }

    private static String getWindowsConsoleEncoding() {
        String encoding = "cp1252"; // hopefully a reasonable a priori value
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "chcp");
        try {
            Process process = pb.start();
            process.waitFor();
            int maxSize = 120;
            byte[] bytes = new byte[maxSize];
            try (InputStream inputStream = process.getInputStream()) {
                int b;
                int i = 0;
                while ((b = inputStream.read()) >= 0 && i < maxSize) {
                    bytes[i] = (byte) b;
                    i++;
                }
            }
            String output = new String(bytes); // default encoding ok (only digits needed)
            Matcher matcher = DIGITS_PATTERN.matcher(output);
            if (matcher.find()) {
                encoding = "cp".concat(output.substring(matcher.start(), matcher.end()));
            }
        } catch (IOException e) {
            // no action - return the a priori value
        } catch (InterruptedException e) {
            // no action - return the a priori value
        }
        return encoding;
    }

}
