package com.kkambbak.client.azure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkambbak.client.azure.dto.AzurePronunciationDto;
import com.kkambbak.client.azure.exception.PronunciationFailException;
import com.kkambbak.client.azure.exception.PronunciationUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzurePronunciationService {

    @Value("${azure.api.key:dummy_azure_key}")
    private String azureApiKey;

    private static final String REGION = "koreacentral";
    private static final String LANG = "ko-KR";
    private static final ObjectMapper mapper = new ObjectMapper();

    public AzurePronunciationDto getPronunciationScore(String referenceText, File wavFile) {

        try {
            // WAV 파일 읽기
            byte[] audioBytes = Files.readAllBytes(wavFile.toPath());

            // PronunciationAssessment JSON 생성
            String paJson = mapper.createObjectNode()
                    .put("ReferenceText", referenceText)
                    .put("GradingSystem", "HundredMark")
                    .put("Granularity", "Phoneme")
                    .put("Dimension", "Comprehensive")     // 종합 발음 평가
                    .put("EnableProsodyAssessment", false)
                    .toString();


            String paBase64 = Base64.getEncoder().encodeToString(paJson.getBytes(StandardCharsets.UTF_8));


            String endpoint = String.format(
                    "https://%s.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1" +
                            "?language=%s&format=detailed",
                    REGION, LANG
            );


            //REST 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Ocp-Apim-Subscription-Key", azureApiKey)
                    .header("Content-Type", "audio/wav; codecs=audio/pcm; samplerate=16000")
                    .header("Pronunciation-Assessment", paBase64)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(audioBytes))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new PronunciationUnavailableException();
            }

            //Json 파싱
            JsonNode root = mapper.readTree(response.body());
            JsonNode nbest = root.path("NBest");
            if (!nbest.isArray() || nbest.size() == 0) {
                throw new PronunciationFailException();
            }

            JsonNode first = nbest.get(0);

            String recognizedText = first.path("Display").asText("");

            double accuracyScore = first.path("AccuracyScore").asDouble(0.0);
            double fluencyScore = first.path("FluencyScore").asDouble(0.0);
            double completenessScore = first.path("CompletenessScore").asDouble(0.0);
            double pronunciationScore = first.path("PronScore").asDouble(0.0);


            if (accuracyScore == 0.0 &&
                    fluencyScore == 0.0 &&
                    completenessScore == 0.0 &&
                    pronunciationScore == 0.0) {

                log.warn("[Azure Warning] Suspicious zero scores returned. Raw response: {}", response.body());
            }


            return AzurePronunciationDto.builder()
                    .text(recognizedText)
                    .accuracyScore(accuracyScore)
                    .fluencyScore(fluencyScore)
                    .completenessScore(completenessScore)
                    .pronunciationScore(pronunciationScore)
                    .build();

        } catch (PronunciationFailException | PronunciationUnavailableException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected Azure error: {}", e.getMessage());
            throw new PronunciationUnavailableException();
        }
    }
}
