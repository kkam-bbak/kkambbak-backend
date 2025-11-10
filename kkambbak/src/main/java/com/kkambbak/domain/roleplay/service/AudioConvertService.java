package com.kkambbak.domain.roleplay.service;

import com.kkambbak.domain.roleplay.exception.FFmegConvertFailException;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioConvertService {

    @Value("${media.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    public File toWav(MultipartFile file) throws IOException {
        File in = File.createTempFile("in-", "-" + Objects.requireNonNullElse(file.getOriginalFilename(), "audio"));
        File out = File.createTempFile("out-", ".wav");
        file.transferTo(in);

        try{
            FFmpeg fFmpeg = new FFmpeg(ffmpegPath);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(in.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(out.getAbsolutePath())
                        .setFormat("wav")
                        .setAudioCodec("pcm_s16le")
                        .setAudioChannels(1)
                        .setAudioSampleRate(16_000)
                        .done();

            new FFmpegExecutor(fFmpeg).createJob(builder).run();
            return out;
        }catch (Exception e){
            out.delete();
            log.error("Fail to convert audio file to .wav");
            throw new FFmegConvertFailException();
        }finally {
            in.delete();
        }
    }

}
