package com.ruoyi.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%?";

    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword() {
        // 确保密码包含至少一个字符来自每个字符集
        List<Character> password = new ArrayList<>();
        password.add(randomChar(LOWERCASE));
        password.add(randomChar(UPPERCASE));
        password.add(randomChar(DIGITS));
        password.add(randomChar(SPECIALS));

        // 填充剩余4个字符
        String allChars = LOWERCASE + UPPERCASE + DIGITS + SPECIALS;
        for (int i = 0; i < 4; i++) {
            password.add(randomChar(allChars));
        }

        // 打乱字符顺序
        Collections.shuffle(password, random);

        // 转换为字符串
        StringBuilder sb = new StringBuilder();
        for (char c : password) {
            sb.append(c);
        }

        return sb.toString();
    }

    private static char randomChar(String characterSet) {
        return characterSet.charAt(random.nextInt(characterSet.length()));
    }

    /**
     * 基础Base64加密
     * @param password 明文密码
     * @return Base64加密后的字符串
     */
    public static String encode(String password) {
        return encoder.encodeToString(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 基础Base64解密
     * @param encodedPassword 加密后的密码
     * @return 解密后的明文密码
     */
    public static String decode(String encodedPassword) {
        byte[] decodedBytes = decoder.decode(encodedPassword);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        // 生成10个随机密码示例
        for (int i = 0; i < 10; i++) {
            System.out.println("生成的密码: " + generatePassword());
        }
    }
}
