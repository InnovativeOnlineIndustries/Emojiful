package com.hrznstudio.emojiful.api;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class EmojiCache {
    private static final Path CACHE_DIRECTORY = Path.of("emojiful", "cache").toAbsolutePath().normalize();
    private static final Pattern SAFE_NAME = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._-]{0,119}");

    private EmojiCache() {
    }

    public static File resolve(String name, int version) {
        String fileName = safeFileName(name) + "-" + version;
        Path cacheFile = CACHE_DIRECTORY.resolve(fileName).normalize();
        if (!cacheFile.startsWith(CACHE_DIRECTORY)) {
            throw new IllegalArgumentException("Emoji cache path escapes the cache directory, malicious server datapack");
        }
        return cacheFile.toFile();
    }

    static String safeFileName(String name) {
        if (name != null && SAFE_NAME.matcher(name).matches()) {
            return name;
        }
        return "emoji-" + sha256(name == null ? "" : name);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
