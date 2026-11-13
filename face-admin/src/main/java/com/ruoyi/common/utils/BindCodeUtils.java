package com.ruoyi.common.utils;

import com.ruoyi.common.exception.ServiceException;

import java.security.SecureRandom;
import java.util.function.Predicate;

public final class BindCodeUtils {

    private static final char[] CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private BindCodeUtils() {
    }

    public static String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    public static String generateUnique(Predicate<String> exists) {
        for (int i = 0; i < 30; i++) {
            String code = generate();
            if (!exists.test(code)) {
                return code;
            }
        }
        throw new ServiceException("生成绑定码失败，请重试");
    }
}
