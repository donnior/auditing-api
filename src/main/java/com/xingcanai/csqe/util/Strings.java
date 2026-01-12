package com.xingcanai.csqe.util;

import java.util.Base64;
import java.util.function.Consumer;

public class Strings {

    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    public static String nullToEmpty(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static void ifNotEmpty(String str, Consumer<String> consumer) {
        if (isNotEmpty(str)) {
            consumer.accept(str);
        }
    }

    public static String base64Decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    public static String base64Decode(String str, boolean safe) {
        if (isEmpty(str)) {
            return "";
        }
        if (safe) {
            try {
                return new String(Base64.getDecoder().decode(str));
            } catch (Exception e) {
                System.out.println(e);
                return str;
            }
        }
        return base64Decode(str);
    }

    public static void main(String[] args) {
        System.out.println(base64Decode("5pyo55+z"));
    }
}
