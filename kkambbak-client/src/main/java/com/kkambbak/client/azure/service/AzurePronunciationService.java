package com.kkambbak.client.azure.service;

import com.kkambbak.client.azure.dto.AzurePronunciationDto;
import com.kkambbak.client.azure.exception.PronunciationFailException;
import com.kkambbak.client.azure.exception.PronunciationUnavailableException;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzurePronunciationService {

    @Value("${azure.api.key:dummy_azure_key}")
    private String azureApikey;

    private final String speechRegion = "koreacentral";
    private final String speechLang = "ko-KR";

    public AzurePronunciationDto getPronunciationScore(String referenceText, File wavFile) {
        try (SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureApikey, speechRegion)) {
            speechConfig.setSpeechRecognitionLanguage(speechLang);

            try (AudioConfig audioConfig = AudioConfig.fromWavFileInput(wavFile.getAbsolutePath());
                 SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig)) {

                PronunciationAssessmentConfig paConfig = new PronunciationAssessmentConfig(
                        referenceText,
                        PronunciationAssessmentGradingSystem.HundredMark, //0~100 점수체계
                        PronunciationAssessmentGranularity.Phoneme, //음소 단위
                        true //미스큐 감지 활성화하여 발음뿐만 아니라 기준 문장 vs 실제 발화 비교하도록 설정
                );
                paConfig.applyTo(recognizer);
                SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();

                if (result.getReason() != ResultReason.RecognizedSpeech) {
                    if (result.getReason() == ResultReason.Canceled) {
                        CancellationDetails cancellation = CancellationDetails.fromResult(result);
                        log.warn("Recognition canceled: Reason={}, Error={}",
                                cancellation.getReason(), cancellation.getErrorDetails());
                        throw new PronunciationUnavailableException();
                    } else {
                        log.warn("Recognition not successful: reason={}", result.getReason());
                        throw new PronunciationFailException();
                    }

                }

                PronunciationAssessmentResult pa = PronunciationAssessmentResult.fromResult(result);

                return AzurePronunciationDto.builder()
                        .text(result.getText())
                        .accuracyScore(safe(pa.getAccuracyScore()))
                        .fluencyScore(safe(pa.getFluencyScore()))
                        .completenessScore(safe(pa.getCompletenessScore()))
                        .pronunciationScore(safe(pa.getPronunciationScore()))
                        .build();

            }catch (PronunciationFailException e) {
                throw e;
            }
            catch (Exception e) {
                log.error("Pronunciation scoring failed", e);
                throw new PronunciationFailException();}
        }catch (Exception e) {
            log.error("Azure SpeechConfig initialization failed", e);
            throw new PronunciationUnavailableException();
        }
    }

    private static double safe(Double v) {
        return v == null ? 0.0 : v;
    }
}
