package com.kkambbak.global.util;

import com.kkambbak.domain.upload.exception.ImageConversionFailedException;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


@Slf4j
public class ImageConverter {

    private static final int DEFAULT_QUALITY = 80;
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;

    public static InputStream convertToWebP(MultipartFile file) {
        return convertToWebP(file, DEFAULT_QUALITY);
    }

    public static InputStream convertToWebP(MultipartFile file, int quality) {
        try (InputStream inputStream = file.getInputStream()) {
            return convertToWebP(inputStream, quality, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to convert image to WebP: {}", file.getOriginalFilename(), e);
            throw new ImageConversionFailedException(file.getOriginalFilename());
        }
    }

    public static InputStream convertToWebP(InputStream inputStream) {
        return convertToWebP(inputStream, DEFAULT_QUALITY);
    }

    public static InputStream convertToWebP(InputStream inputStream, int quality) {
        return convertToWebP(inputStream, quality, "input-stream");
    }

    private static InputStream convertToWebP(InputStream inputStream, int quality, String filename) {
        try {
            try {
                ImmutableImage image = ImmutableImage.loader().fromStream(inputStream);
                return processImageConversion(image, quality);
            } catch (Exception e) {
                inputStream = convertViaPng(inputStream, filename);
                ImmutableImage image = ImmutableImage.loader().fromStream(inputStream);
                return processImageConversion(image, quality);
            }
        } catch (Exception e) {
            log.error("Failed to convert image to WebP: {}", filename, e);
            throw new ImageConversionFailedException(filename);
        }
    }

    private static InputStream convertViaPng(InputStream inputStream, String filename) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        if (bufferedImage == null) {
            throw new IllegalArgumentException("Failed to read image: " + filename);
        }

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", pngOutputStream);

        return new ByteArrayInputStream(pngOutputStream.toByteArray());
    }

    // 이미지 처리 및 변환
    private static InputStream processImageConversion(ImmutableImage image, int quality) throws Exception {
        image = resizeIfNeeded(image);
        return convertImageToWebP(image, quality);
    }

    // 이미지 리사이징이 필요한 경우 리사이징 수행
    private static ImmutableImage resizeIfNeeded(ImmutableImage image) {
        if (image.width > MAX_WIDTH || image.height > MAX_HEIGHT) {
            int originalWidth = image.width;
            int originalHeight = image.height;

            double scale = Math.min((double) MAX_WIDTH / image.width, (double) MAX_HEIGHT / image.height);
            int newWidth = (int) (image.width * scale);
            int newHeight = (int) (image.height * scale);

            image = image.scaleTo(newWidth, newHeight);
        }
        return image;
    }

    private static InputStream convertImageToWebP(ImmutableImage image, int quality) throws Exception {
        WebpWriter writer = WebpWriter.DEFAULT.withQ(quality);
        byte[] webpBytes = image.bytes(writer);
        return new ByteArrayInputStream(webpBytes);
    }
}