package com.kkambbak.domain.learning.service;

import com.kkambbak.client.azure.dto.AzurePronunciationDto;
import com.kkambbak.client.azure.exception.PronunciationFailException;
import com.kkambbak.client.azure.exception.PronunciationUnavailableException;
import com.kkambbak.client.azure.service.AzurePronunciationService;
import com.kkambbak.core.entity.learning.LearningResult;
import com.kkambbak.core.entity.learning.LearningResultDetail;
import com.kkambbak.core.entity.learning.Session;
import com.kkambbak.core.entity.learning.Vocabulary;
import com.kkambbak.core.entity.learning.enums.GradeAction;
import com.kkambbak.core.repository.learning.LearningResultDetailsRepository;
import com.kkambbak.core.repository.learning.LearningResultRepository;
import com.kkambbak.core.repository.learning.SessionRepository;
import com.kkambbak.core.repository.learning.SessionVocabularyRepository;
import com.kkambbak.core.repository.learning.VocabularyRepository;
import com.kkambbak.domain.learning.dto.LearningGradeDto;
import com.kkambbak.domain.learning.exception.InvalidGradeAttemptException;
import com.kkambbak.domain.learning.exception.LearningDataInconsistencyException;
import com.kkambbak.domain.learning.exception.ResultNotFoundException;
import com.kkambbak.domain.learning.exception.SessionNotFoundException;
import com.kkambbak.domain.roleplay.service.AudioConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningGradeService {

    private final LearningResultRepository learningResultRepository;
    private final LearningResultDetailsRepository detailsRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SessionRepository sessionRepository;
    private final AzurePronunciationService pronunciationService;

    private static final double PASSING_PRONUNCIATION_SCORE = 60.0;

    public void validateBasicInputs(LearningGradeDto.GradeRequest req,
                                    MultipartFile audioFile) {
        if (req.getAction() == null) {
            throw new InvalidGradeAttemptException("action은 필수입니다.");
        }
        if (req.getItemId() == null) {
            throw new InvalidGradeAttemptException("itemId는 필수입니다.");
        }

        if (req.getAction() == GradeAction.GRADE) {
            if (audioFile == null || audioFile.isEmpty()) {
                throw new InvalidGradeAttemptException("GRADE 모드에서는 audioFile이 필수입니다.");
            }
        }
    }

    // 세션 로드
    public Session loadSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "세션을 찾을 수 없습니다. sessionId=" + sessionId
                ));
    }

    // 유저 + 세션 기준 최신 LearningResult
    public LearningResult findLatestResult(Long userId, Long sessionId) {
        List<LearningResult> results =
                learningResultRepository.findByUserIdAndSessionIds(userId, List.of(sessionId));

        return results.stream()
                .max(Comparator.comparingLong(LearningResult::getId))
                .orElseThrow(() -> new ResultNotFoundException(
                        "학습 결과를 찾을 수 없습니다. userId=%d, sessionId=%d"
                                .formatted(userId, sessionId)
                ));
    }

    // 세션 단어 순서 로드
    public List<Long> loadVocabOrder(Long sessionId) {
        List<Long> vocabIds = sessionVocabularyRepository
                .findVocabularyIdsOrderByVocabIdAsc(sessionId);

        if (vocabIds.isEmpty()) {
            throw new LearningDataInconsistencyException(
                    "세션에 연결된 단어가 없습니다. sessionId=" + sessionId
            );
        }
        return vocabIds;
    }

    // 현재 단어의 index
    public int findOrderIndex(List<Long> vocabIds, Long itemId) {
        int index = vocabIds.indexOf(itemId);

        if (index == -1) {
            throw new InvalidGradeAttemptException(
                    "세션에 없는 단어입니다. itemId=" + itemId
            );
        }

        return index;
    }

    // Vocabulary 로딩
    public Vocabulary loadVocabulary(Long vocabId) {
        return vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new LearningDataInconsistencyException(
                        "학습 단어를 찾을 수 없습니다. vocabularyId=" + vocabId
                ));
    }

    /**
     * Azure STT + 발음 평가 기반 정답 여부 판단
     * (NEXT_AFTER_WRONG인 경우는 오답 처리 후 다음 문제로 넘어가기 때문에 평가하지 않음)
     */
    public boolean evaluateCorrectness(
            GradeAction action,
            MultipartFile audioFile,
            Vocabulary vocab
    ) {
        if (action == GradeAction.NEXT_AFTER_WRONG) {
            return false;
        }
        if (audioFile == null || audioFile.isEmpty()) {
            throw new InvalidGradeAttemptException("audioFile이 필요합니다.");
        }
        return evaluatePronunciationWithAzure(audioFile, vocab);
    }

    // 실제 발음 평가 로직
    private boolean evaluatePronunciationWithAzure(
            MultipartFile audioFile,
            Vocabulary vocab
    ) {
        File wavFile = null;
        try {
            wavFile = File.createTempFile("kkambbak-pron-", ".wav");
            audioFile.transferTo(wavFile.toPath());

            String referenceText = vocab.getKorean();

            AzurePronunciationDto result =
                    pronunciationService.getPronunciationScore(referenceText, wavFile);
            if (result == null) {
                throw new PronunciationFailException();
            }

            double score = result.getPronunciationScore();

            return score >= PASSING_PRONUNCIATION_SCORE;

        } catch (IOException e) {
            log.error("음성 파일 처리 중 오류 발생", e);
            throw new PronunciationUnavailableException();

        } finally {
            if (wavFile != null) {
                try {
                    Files.deleteIfExists(wavFile.toPath());
                } catch (IOException ex) {
                    log.warn("임시 WAV 파일 삭제 중 오류: {}", wavFile.getAbsolutePath(), ex);
                }
            }
        }
    }

    // 채점 결과 반영 (상세 저장 + 정답 카운트 증가)
    @Transactional
    public void applyGradingResult(
            LearningResult result,
            Vocabulary vocab,
            GradeAction action,
            boolean isCorrect
    ) {
        saveDetail(result, vocab, isCorrect, null);

        if (action == GradeAction.GRADE && isCorrect) {
            result.addCorrectCount();
        }
    }

    // 기존 detail 있으면 update, 없으면 insert
    private void saveDetail(
            LearningResult result,
            Vocabulary vocab,
            boolean correct,
            String userAnswer
    ) {
        Optional<LearningResultDetail> optional =
                detailsRepository.findByLearningResultIdAndVocabularyId(
                        result.getId(),
                        vocab.getId()
                );
        if (optional.isPresent()) {
            LearningResultDetail existing = optional.get();
            existing.update(correct, userAnswer);
            return;
        }
        LearningResultDetail detail = LearningResultDetail.builder()
                .learningResult(result)
                .vocabulary(vocab)
                .correct(correct)
                .userAnswer(userAnswer)
                .build();
        detailsRepository.save(detail);
    }
}