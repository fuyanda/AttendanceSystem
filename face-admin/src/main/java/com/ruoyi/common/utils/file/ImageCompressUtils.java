package com.ruoyi.common.utils.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Image compression: scale down so long-side <= maxSide, then JPEG encode at given quality.
 */
public class ImageCompressUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageCompressUtils.class);

    /** Max pixel of the longer dimension */
    public static final int DEFAULT_MAX_SIDE = 1280;
    /** JPEG compression quality 0.0-1.0 */
    public static final float DEFAULT_QUALITY = 0.82f;

    private ImageCompressUtils() {}

    /**
     * Compress image bytes.
     *
     * @param input   original bytes
     * @param maxSide max long-side (px); 0 = no resize
     * @param quality JPEG quality 0.0-1.0
     * @return compressed bytes; returns input unchanged on failure
     */
    public static byte[] compress(byte[] input, int maxSide, float quality) {
        try (InputStream is = new ByteArrayInputStream(input)) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                return input;
            }
            int w = img.getWidth();
            int h = img.getHeight();

            if (maxSide > 0) {
                int longSide = Math.max(w, h);
                if (longSide > maxSide) {
                    double scale = (double) maxSide / longSide;
                    int nw = (int) Math.round(w * scale);
                    int nh = (int) Math.round(h * scale);
                    BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = scaled.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, nw, nh);
                    g2d.drawImage(img, 0, 0, nw, nh, null);
                    g2d.dispose();
                    img = scaled;
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            try (MemoryCacheImageOutputStream mos = new MemoryCacheImageOutputStream(out)) {
                writer.setOutput(mos);
                writer.write(null, new IIOImage(img, null, null), param);
            } finally {
                writer.dispose();
            }
            byte[] result = out.toByteArray();
            log.debug("compress: {}B -> {}B ({}x{} -> {}x{})",
                    input.length, result.length, w, h, img.getWidth(), img.getHeight());
            return result;
        } catch (IOException e) {
            log.warn("Image compression failed, using original: {}", e.getMessage());
            return input;
        }
    }

    public static byte[] compress(byte[] input) {
        return compress(input, DEFAULT_MAX_SIDE, DEFAULT_QUALITY);
    }
}
