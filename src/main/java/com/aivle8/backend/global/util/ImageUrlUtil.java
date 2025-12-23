package com.aivle8.backend.global.util;

public class ImageUrlUtil {
    private static final String S3_BASE_URL = "https://assets.example.invalid/";

    public static String toFullUrl(String path) {
        if (path != null && !path.isEmpty() && !path.startsWith("http")) {
            return S3_BASE_URL + path;
        }
        return path;
    }
}