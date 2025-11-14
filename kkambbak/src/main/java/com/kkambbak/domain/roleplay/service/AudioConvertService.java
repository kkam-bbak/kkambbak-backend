package com.kkambbak.domain.roleplay.service;

import com.kkambbak.domain.roleplay.exception.FFmpegConvertFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioConvertService {

    @Value("${media.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    private static final String OUTPUT_FORMAT = "wav";
    private static final String AUDIO_CODEC = "pcm_s16le";
    private static final int AUDIO_CHANNELS = 1; // 모노
    private static final int SAMPLE_RATE = 16000; // 16kHz


    public File toWav(MultipartFile file) throws IOException {
        File in = null;
        File out = null;

        try {
            in = File.createTempFile("in-", "-" + Objects.requireNonNullElse(file.getOriginalFilename(), "audio"));
            out = File.createTempFile("out-", ".wav");
            file.transferTo(in);

            FFmpeg fFmpeg = new FFmpeg(ffmpegPath);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(in.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(out.getAbsolutePath())
                    .setFormat(OUTPUT_FORMAT)
                    .setAudioCodec(AUDIO_CODEC)
                    .setAudioChannels(AUDIO_CHANNELS)
                    .setAudioSampleRate(SAMPLE_RATE)
                    .done();

            new FFmpegExecutor(fFmpeg).createJob(builder).run();

            return out;

        } catch (Exception e) {
            log.error("Fail to convert audio file to .wav", e);
            if (out != null) safeDelete(out);

            throw new FFmpegConvertFailException();

        } finally {
            if (in != null) safeDelete(in);
        }
    }


    private void safeDelete(File file) {
        try {
            if (file != null && file.exists()) {
                Files.deleteIfExists(file.toPath());
            }
        } catch (IOException ex) {
            log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
        }
    }

}
