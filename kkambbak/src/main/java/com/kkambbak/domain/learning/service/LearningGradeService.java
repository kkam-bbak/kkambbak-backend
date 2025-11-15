package com.kkambbak.domain.learning.service;

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
import com.kkambbak.domain.learning.exception.LearningResultNotFoundException;
import com.kkambbak.domain.learning.exception.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningGradeService {

    private final LearningResultRepository learningResultRepository;
    private final LearningResultDetailsRepository detailsRepository;
    private final SessionVocabularyRepository sessionVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SessionRepository sessionRepository;

    // grade 흐름에서 사용하는 내부 결과 묶음
    private record FlowDecision(
            boolean moved,
            boolean finished,
            LearningGradeDto.Next next,
            LearningGradeDto.CorrectAnswer correctAnswer
    ) {}

    @Transactional
    public LearningGradeDto.GradeResponse grade(Long userId,
                                                Long sessionId,
                                                LearningGradeDto.GradeRequest req) {

        // 1) 기본 필수 필드 검증
        validateBasicInputs(req);

        // 2) 세션 / 최신 결과 / 세션 단어 순서 / 현재 단어 로딩
        Session session = loadSession(sessionId); // 세션이 실제로 존재하는지 검증
        LearningResult result = findLatestResult(userId, sessionId);
        List<Long> vocabIds = loadVocabOrder(sessionId);
        Vocabulary currentVocab = loadVocabulary(req.getItemId());

        int orderIndex = findOrderIndex(vocabIds, req.getItemId());

        // 3) 정답 여부 결정 (더미 채점)
        GradeAction action = req.getAction();
        boolean isCorrect = evaluateCorrectness(action, req, currentVocab);

        // 4) 학습 상세 저장 + 정답 카운트 갱신
        applyGradingResult(result, currentVocab, action, isCorrect);

        // 5) 흐름 결정 (moved / finished / next / correctAnswer)
        boolean isLast = isLastItem(orderIndex, vocabIds.size());
        FlowDecision flow = decideFlow(action, isCorrect, isLast, orderIndex, vocabIds, currentVocab);

        // 6) 완료 처리
        if (flow.finished()) {
            result.complete();
        }

        // 9) 최종 응답 반환
        return LearningGradeDto.GradeResponse.of(
                isCorrect,
                flow.moved(),
                flow.finished(),
                flow.next(),
                flow.correctAnswer()
        );
    }

    private void validateBasicInputs(LearningGradeDto.GradeRequest req) {
        if (req.getAction() == null) {
            throw new InvalidGradeAttemptException("action은 필수입니다.");
        }
        if (req.getItemId() == null) {
            throw new InvalidGradeAttemptException("itemId는 필수입니다.");
            }
    }

    private Session loadSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "세션을 찾을 수 없습니다. sessionId=" + sessionId
                ));
    }

    private LearningResult findLatestResult(Long userId, Long sessionId) {
        List<LearningResult> results =
                learningResultRepository.findByUserIdAndSessionIds(userId, List.of(sessionId));

        return results.stream()
                .max(Comparator.comparingLong(LearningResult::getId))
                .orElseThrow(() -> new LearningResultNotFoundException(
                        "학습 결과를 찾을 수 없습니다. userId=%d, sessionId=%d"
                                .formatted(userId, sessionId)
                ));
    }

    // 세션 단어 순서 로드
    private List<Long> loadVocabOrder(Long sessionId) {
        List<Long> vocabIds = sessionVocabularyRepository
                .findVocabularyIdsOrderByVocabIdAsc(sessionId);

        if (vocabIds.isEmpty()) {
            throw new LearningDataInconsistencyException(
                    "세션에 연결된 단어가 없습니다. sessionId=" + sessionId
            );
        }
        return vocabIds;
    }

    // 현재 단어가 세션 전체 단어 리스트에서 몇번째인지 찾기 위해
    private int findOrderIndex(List<Long> vocabIds, Long itemId) {
        int index = vocabIds.indexOf(itemId);

        if (index == -1) {
            throw new InvalidGradeAttemptException(
                    "세션에 없는 단어입니다. itemId=" + itemId
            );
        }

        return index;
    }

    private Vocabulary loadVocabulary(Long vocabId) {
        return vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new LearningDataInconsistencyException(
                        "학습 단어를 찾을 수 없습니다. vocabularyId=" + vocabId
                ));
    }

    // 정답 여부 판단 (현재는 더미 채점)
    private boolean evaluateCorrectness(GradeAction action,
                                        LearningGradeDto.GradeRequest req,
                                        Vocabulary vocab) {

        return switch (action) {
            case GRADE -> dummyGrading(req, vocab);
            case NEXT_AFTER_WRONG -> false;
        };
    }

    // 더미 채점 로직
    private boolean dummyGrading(LearningGradeDto.GradeRequest req, Vocabulary vocab) {
        // ✅ 임시 규칙:
        //  - audio == "wrong" 이면 오답
        //  - 그 외( null 포함 )는 전부 정답
        String audio = req.getAudio();
        if ("wrong".equalsIgnoreCase(audio)) {
            return false;   // ❌ 일부러 틀린 케이스
        }
        return true;        // ✅ 기본은 정답
    }

    //  4) 채점 결과 반영: 상세 저장 + 정답 카운트 증가
    private void applyGradingResult(LearningResult result,
                                    Vocabulary vocab,
                                    GradeAction action,
                                    boolean isCorrect) {
        saveDetail(result, vocab, isCorrect, "dummy"); // userAnswer는 임시 값

        if (action == GradeAction.GRADE && isCorrect) {
            result.addCorrectCount();
        }
    }

    // 기존 결과가 있는지 조회 후 있으면 update, 없으면 insert
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

    //  마지막 단어인지 여부
    private boolean isLastItem(int orderIndex, int totalSize) {
        return orderIndex == totalSize - 1;
    }

    /**
     * action(GRADE / NEXT_AFTER_WRONG)에 따라
     * 어떤 흐름 로직을 실행할지를 결정하는 상위 분기 메서드.
     *
     * "행동(action) → 로직 매핑"만 담당하고,
     * 세부 흐름 결정 로직은 각각의 하위 메서드가 담당한다.
     */
    private FlowDecision decideFlow(GradeAction action,
                                    boolean isCorrect,
                                    boolean isLast,
                                    int orderIndex,
                                    List<Long> vocabIds,
                                    Vocabulary currentVocab) {

        return switch (action) {
            case GRADE ->
                    decideFlowForGrade(isCorrect, isLast, orderIndex, vocabIds);

            case NEXT_AFTER_WRONG ->
                    decideFlowForNextAfterWrong(isLast, orderIndex, vocabIds, currentVocab);
        };
    }

    /**
     * action = GRADE(채점)
     * "사용자의 발음이 정답인지/오답인지"를 기준으로 흐름을 결정하는 역할
     *
     * 처리 흐름
     * 1) 오답인 경우 => 같은 단어에서 재학습
     * 2) 정답 + 마지막 단어인 경우 => 라운드 전체 종료
     * 3) 정답 + 다음 단어 존재하는 경우 => 다음 단어 정보 주기
     *
     */
    private FlowDecision decideFlowForGrade(boolean isCorrect,
                                            boolean isLast,
                                            int orderIndex,
                                            List<Long> vocabIds) {

        // 오답 → 현재 단어 유지
        if (!isCorrect) {
            return new FlowDecision(false, false, null, null);
        }

        // 정답 + 마지막 단어 → 종료
        if (isLast) {
            return new FlowDecision(true, true, null, null);
        }

        // 정답 + 다음 단어 존재
        int nextIndex = orderIndex + 1;
        LearningGradeDto.Next next = buildNextDto(nextIndex, vocabIds);

        return new FlowDecision(true, false, next, null);
    }


    /**
     * action = NEXT_AFTER_WRONG (오답 확정 후 다음 단어로 이동하기)
     *
     * 오답 후 “Next” 버튼을 눌렀을 때 실행되는 흐름.
     * 즉, 이 메서드는 항상 "오답을 확정하고, 다음 단어로 이동"시킨다.
     *
     * 처리 흐름
     * 1) 마지막 단어인 경우 => 마지막 단어 오답 → 정답 보여주고 학습 종료
     * 2) 마지막이 아닌 경우 => "정답 공개 → 다음 단어 진입" 흐름
     */
    private FlowDecision decideFlowForNextAfterWrong(boolean isLast,
                                                     int orderIndex,
                                                     List<Long> vocabIds,
                                                     Vocabulary currentVocab) {

        // 정답 공개용 DTO
        LearningGradeDto.CorrectAnswer correctAnswer = buildCorrectAnswerDto(currentVocab);

        // 마지막 단어면 종료
        if (isLast) {
            return new FlowDecision(true, true, null, correctAnswer);
        }

        // 다음 단어로 이동
        int nextIndex = orderIndex + 1;
        LearningGradeDto.Next next = buildNextDto(nextIndex, vocabIds);

        return new FlowDecision(true, false, next, correctAnswer);
    }


    // 다음 단어 DTO 생성
    private LearningGradeDto.Next buildNextDto(int nextIndex, List<Long> vocabIds) {
        Long nextVocabId = vocabIds.get(nextIndex);
        Vocabulary nextVocab = loadVocabulary(nextVocabId);

        return LearningGradeDto.Next.builder()
                .itemId(nextVocabId)
                .korean(nextVocab.getKorean())
                .english(nextVocab.getEnglish())
                .romanization(nextVocab.getRomanized())
                .imageUrl(nextVocab.getImageUrl())
                .build();
    }


    // 오답 확정 시 정답 공개용 DTO 생성
    private LearningGradeDto.CorrectAnswer buildCorrectAnswerDto(Vocabulary vocab) {
        return LearningGradeDto.CorrectAnswer.builder()
                .itemId(vocab.getId())
                .korean(vocab.getKorean())
                .romanization(vocab.getRomanized())
                .english(vocab.getEnglish())
                .imageUrl(vocab.getImageUrl())
                .build();
    }


}