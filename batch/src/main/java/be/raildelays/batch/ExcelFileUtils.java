/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Almex
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package be.raildelays.batch;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class helpful to build all what is related to Excel file location/path/name.
 *
 * @author Almex
 * @since 1.2
 */
public class ExcelFileUtils extends FileUtils {

    public static File getFile(File directory, String prefix, LocalDate date, String extension) throws IOException {
        assert date != null : "You must provide a date!";

        String suffix = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return getFile(directory, prefix, suffix, extension);
    }


    public static File getFile(File directory, String prefix, String suffix, String extension) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append(prefix);
        builder.append(" ");
        builder.append(suffix);
        builder.append(extension);

        return new File(directory, builder.toString());
    }

    public static String getFileName(File file) {
        String originalFileName = file.getName();
        int extensionIndex = originalFileName.lastIndexOf(".");

        return originalFileName.substring(0, extensionIndex);
    }

    public static String getFileExtension(File file) {
        String originalFileName = file.getName();
        int extensionIndex = originalFileName.lastIndexOf(".");

        return originalFileName.substring(extensionIndex);
    }
}
