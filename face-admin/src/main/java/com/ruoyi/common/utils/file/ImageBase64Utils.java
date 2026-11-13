package com.ruoyi.common.utils.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Base64图片处理工具类
 */
public class ImageBase64Utils {

    /**
     * 将Base64图片字符串保存为文件
     * @param base64Image Base64编码的图片字符串（可能包含前缀）
     * @param outputPath 输出文件路径（包含文件名和扩展名）
     * @return 是否保存成功
     */
    public static boolean saveBase64Image(String base64Image, String outputPath) {
        if (base64Image == null || base64Image.isEmpty()) {
            return false;
        }

        try {
            String cleanedBase64 = base64Image.replaceAll("\\s+", "");
            // 处理可能存在的Base64前缀
            String imageData = extractPureBase64Data(cleanedBase64);

            // 解码Base64
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            // 确保目录存在
            File outputFile = new File(outputPath);
            createParentDirs(outputFile);

            // 写入文件
            writeBytesToFile(imageBytes, outputFile);

            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("无效的Base64字符串: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("文件操作错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从可能包含前缀的Base64字符串中提取纯Base64数据
     * @param base64Data 可能包含前缀的Base64字符串
     * @return 纯Base64数据
     */
    private static String extractPureBase64Data(String base64Data) {
        // Android生成的Base64字符串通常不带前缀，但这里做兼容处理
        if (base64Data.startsWith("data:image/")) {
            int base64Index = base64Data.indexOf("base64,");
            if (base64Index > 0) {
                return base64Data.substring(base64Index + 7);
            }
        }
        return base64Data;
    }

    /**
     * 创建文件的父目录
     * @param file 目标文件
     */
    private static void createParentDirs(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("无法创建目录: " + parent.getAbsolutePath());
            }
        }
    }

    /**
     * 将字节数组写入文件
     * @param data 字节数组
     * @param file 目标文件
     */
    private static void writeBytesToFile(byte[] data, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    /**
     * 获取Base64字符串的图片格式（根据前缀判断）
     * @param base64Image Base64编码的图片字符串
     * @return 图片格式（如jpg、png等），无法确定时返回null
     */
    public static String detectImageFormat(String base64Image) {
        if (base64Image.startsWith("data:image/")) {
            int formatEnd = base64Image.indexOf(";");
            if (formatEnd > 0) {
                return base64Image.substring(11, formatEnd);
            }
        }
        return null;
    }
}
