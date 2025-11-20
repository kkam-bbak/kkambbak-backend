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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzurePronunciationService {

    @Value("${azure.api.key:dummy_azure_key}")
    private String azureApikey;

    private static final String SPEECH_REGION = "koreacentral";
    private static final String SPEECH_LANG = "ko-KR";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AzurePronunciationDto getPronunciationScore(String referenceText, File wavFile) {
        try {
            log.debug("Azure API Key configured: {}", azureApikey != null && !azureApikey.isEmpty());
            log.debug("Region: {}, Language: {}", SPEECH_REGION, SPEECH_LANG);

            // WAV 파일 읽기
            byte[] audioBytes;
            try {
                audioBytes = Files.readAllBytes(wavFile.toPath());
            } catch (Exception e) {
                log.error("WAV file read failed: {}", e.getMessage());
                throw new PronunciationFailException();
            }

            // PronunciationAssessment 파라미터 생성
            String paJson;
            try {
                paJson = "{\"referenceText\":\"" + referenceText.replace("\"", "\\\"") + "\"," +
                        "\"gradingSystem\":\"HundredMark\"," +
                        "\"granularity\":\"Phoneme\"," +
                        "\"enableMiscue\":true}";
                URLEncoder.encode(paJson, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Parameter creation failed: {}", e.getMessage());
                throw new PronunciationFailException();
            }

            // REST API 요청
            String endpoint = String.format(
                    "https://%s.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=%s&format=json&PronunciationAssessment=%s",
                    SPEECH_REGION, SPEECH_LANG, paJson.isEmpty() ? "" : URLEncoder.encode(paJson, StandardCharsets.UTF_8)
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Ocp-Apim-Subscription-Key", azureApikey)
                    .header("Content-Type", "audio/wav")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(audioBytes))
                    .build();

            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                log.error("REST API call failed: {}", e.getMessage());
                throw new PronunciationUnavailableException();
            }

            // 응답 검증
            if (response.statusCode() != 200) {
                log.error("API error - Status: {}, Response: {}", response.statusCode(), response.body());
                throw new PronunciationUnavailableException();
            }

            // JSON 응답 파싱
            JsonNode responseJson;
            try {
                responseJson = objectMapper.readTree(response.body());
            } catch (Exception e) {
                log.error("JSON parsing failed: {}", e.getMessage());
                throw new PronunciationFailException();
            }

            // 결과 추출
            try {
                String recognizedText = responseJson.path("DisplayText").asText("");
                JsonNode paResult = responseJson.path("PronunciationAssessment");

                double accuracyScore = paResult.path("AccuracyScore").asDouble(0.0);
                double fluencyScore = paResult.path("FluencyScore").asDouble(0.0);
                double completenessScore = paResult.path("CompletenessScore").asDouble(0.0);
                double pronunciationScore = paResult.path("PronunciationScore").asDouble(0.0);

                log.info("Pronunciation Score - Accuracy: {}, Fluency: {}, Completeness: {}, Pronunciation: {}",
                        accuracyScore, fluencyScore, completenessScore, pronunciationScore);

                return AzurePronunciationDto.builder()
                        .text(recognizedText)
                        .accuracyScore(accuracyScore)
                        .fluencyScore(fluencyScore)
                        .completenessScore(completenessScore)
                        .pronunciationScore(pronunciationScore)
                        .build();

            } catch (Exception e) {
                log.error("Score extraction failed: {}", e.getMessage());
                throw new PronunciationFailException();
            }

        } catch (PronunciationFailException | PronunciationUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new PronunciationUnavailableException();
        }
    }
}
