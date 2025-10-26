package com.kkambbak.domain.survey.service;

import com.kkambbak.domain.survey.dto.SurveyDto;
import com.kkambbak.domain.survey.enums.CategoryType;
import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prioritizer 고정 매핑 규칙 단위 테스트
 * - TOPIK: 난이도 → 토픽 목록
 * - CASUAL: 관심사 → 토픽 목록
 * - 대소문자/공백/널 입력 처리 및 fallback 확인
 */
class PrioritizerTest {

    private final Prioritizer prioritizer = new Prioritizer();

    // -------------------- TOPIK --------------------

    @Test
    @DisplayName("TOPIK | BEGINNER → topik_1, topik_2")
    void topik_beginner() {
        SurveyDto.SurveySaveResponse.Priorities p = prioritizer.buildForTopik(DifficultyLevel.BEGINNER);

        assertEquals(CategoryType.TOPIK, p.getCategoryType());
        assertEquals(DifficultyLevel.BEGINNER, p.getLevel());
        assertEquals(List.of("topik_1", "topik_2"), p.getTopics());
    }

    @Test
    @DisplayName("TOPIK | INTERMEDIATE → topik_3, topik_4")
    void topik_intermediate() {
        var p = prioritizer.buildForTopik(DifficultyLevel.INTERMEDIATE);

        assertEquals(CategoryType.TOPIK, p.getCategoryType());
        assertEquals(DifficultyLevel.INTERMEDIATE, p.getLevel());
        assertEquals(List.of("topik_3", "topik_4"), p.getTopics());
    }

    @Test
    @DisplayName("TOPIK | ADVANCED → topik_5, topik_6")
    void topik_advanced() {
        var p = prioritizer.buildForTopik(DifficultyLevel.ADVANCED);

        assertEquals(CategoryType.TOPIK, p.getCategoryType());
        assertEquals(DifficultyLevel.ADVANCED, p.getLevel());
        assertEquals(List.of("topik_5", "topik_6"), p.getTopics());
    }

    @Test
    @DisplayName("TOPIK | level=null → 기본값 BEGINNER 규칙 사용")
    void topik_nullLevel_defaultsToBeginner() {
        var p = prioritizer.buildForTopik(null);

        assertEquals(CategoryType.TOPIK, p.getCategoryType());
        assertEquals(DifficultyLevel.BEGINNER, p.getLevel());
        assertEquals(List.of("topik_1", "topik_2"), p.getTopics());
    }

    // -------------------- CASUAL --------------------

    @Test
    @DisplayName("CASUAL | daily expressions → emotions, places")
    void casual_dailyExpressions() {
        var p = prioritizer.buildForCasual("daily expressions");

        assertEquals(CategoryType.CASUAL, p.getCategoryType());
        assertNull(p.getLevel()); // CASUAL은 level 미사용
        assertEquals(List.of("emotions", "places"), p.getTopics());
    }

    @Test
    @DisplayName("CASUAL | emotions → emotions")
    void casual_emotions() {
        var p = prioritizer.buildForCasual("emotions");

        assertEquals(CategoryType.CASUAL, p.getCategoryType());
        assertNull(p.getLevel());
        assertEquals(List.of("emotions"), p.getTopics());
    }

    @Test
    @DisplayName("CASUAL | food & travel → fruits, places")
    void casual_foodAndTravel() {
        var p = prioritizer.buildForCasual("food & travel");

        assertEquals(CategoryType.CASUAL, p.getCategoryType());
        assertNull(p.getLevel());
        assertEquals(List.of("fruits", "places"), p.getTopics());
    }

    @Test
    @DisplayName("CASUAL | test-related → body, animals")
    void casual_testRelated() {
        var p = prioritizer.buildForCasual("test-related");

        assertEquals(CategoryType.CASUAL, p.getCategoryType());
        assertNull(p.getLevel());
        assertEquals(List.of("body", "animals"), p.getTopics());
    }

    @Test
    @DisplayName("CASUAL | slang → emotions, colors")
    void casual_slang() {
        var p = prioritizer.buildForCasual("slang");

        assertEquals(CategoryType.CASUAL, p.getCategoryType());
        assertNull(p.getLevel());
        assertEquals(List.of("emotions", "colors"), p.getTopics());
    }

    // -------------------- 입력 정규화 & Fallback --------------------

    @Test
    @DisplayName("CASUAL | 대소문자/양끝 공백 허용 → 정상 매핑")
    void casual_trimAndCaseInsensitive() {
        var p = prioritizer.buildForCasual("   FoOd & TRAVEL  ");
        assertEquals(List.of("fruits", "places"), p.getTopics());
    }

    @Test
    @DisplayName("CASUAL | 알 수 없는 값/빈 값/널 → fallback(emotions, places)")
    void casual_unknownOrEmptyOrNull_fallback() {
        assertEquals(List.of("emotions", "places"), prioritizer.buildForCasual("unknown").getTopics());
        assertEquals(List.of("emotions", "places"), prioritizer.buildForCasual("   ").getTopics());
        assertEquals(List.of("emotions", "places"), prioritizer.buildForCasual(null).getTopics());
    }
}