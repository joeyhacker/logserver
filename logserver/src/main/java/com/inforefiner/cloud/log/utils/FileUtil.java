package com.inforefiner.cloud.log.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FileUtil {

    public static String loadFromClassPath(String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(FileUtil.class.getResourceAsStream(path)));
            final StringBuilder sb = new StringBuilder();
            String s = null;
            String ls = "\r\n";
            boolean first = true;
            while ((s = reader.readLine()) != null) {
                /*
                 * If there's more than one line to be displayed, we need to add a newline to the StringBuilder. For the
                 * first line, we don't do so.
                 */
                if (!first) {
                    sb.append(ls);
                }
                first = false;
                sb.append(s);
            }
            reader.close();
            String content = sb.toString();
            return content;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
