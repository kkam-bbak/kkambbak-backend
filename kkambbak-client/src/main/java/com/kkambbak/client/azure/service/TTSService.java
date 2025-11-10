package com.kkambbak.client.azure.service;


import com.kkambbak.client.azure.exception.TTSSynthesisException;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioOutputStreamCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TTSService {

    @Value("${azure.api.key}")
    private String azureApiKey;

    private final String azureRegion = "koreacentral";

    public byte[] textToSpeech(String korean) {
        if (korean == null || korean.trim().isEmpty()) {
            log.error("TTS Request Text is Empty.");
            throw new TTSSynthesisException("TTS Request Text is Empty.");
        }

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureApiKey, azureRegion);
        speechConfig.setSpeechSynthesisVoiceName("ko-KR-SunHiNeural");
        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz128KBitRateMonoMp3);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PushAudioOutputStreamCallback callback = new PushAudioOutputStreamCallback() {
            @Override
            public int write(byte[] dataBuffer) {
                try {
                    outputStream.write(dataBuffer);
                } catch (Exception e) {
                    log.error("Error while writing audio data.", e);
                    throw new TTSSynthesisException("Failed to process audio data during TTS.");
                }
                return dataBuffer.length;
            }

            @Override
            public void close() {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("Error while closing audio stream.", e);
                }
            }
        };

        AudioOutputStream audioOutputStream = AudioOutputStream.createPushStream(callback);
        AudioConfig audioConfig = AudioConfig.fromStreamOutput(audioOutputStream);

        try (SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig)) {
            SpeechSynthesisResult result = synthesizer.SpeakTextAsync(korean).get();

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                return outputStream.toByteArray();
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails details =
                        SpeechSynthesisCancellationDetails.fromResult(result);
                log.error("TTS request was canceled: {}", details.getErrorDetails());
                throw new TTSSynthesisException("TTS request was canceled: " + details.getErrorDetails());
            } else {
                log.error("Unknown TTS error occurred: reason={}", result.getReason());
                throw new TTSSynthesisException("An unknown error occurred during TTS synthesis.");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception occurred during TTS synthesis.", e);
            Thread.currentThread().interrupt();
            throw new TTSSynthesisException("Exception occurred during TTS synthesis process.");
        } catch (TTSSynthesisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected system error during TTS synthesis.", e);
            throw new TTSSynthesisException("Unexpected system error occurred during TTS synthesis.");
        }
    }
}