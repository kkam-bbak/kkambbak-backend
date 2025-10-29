package com.kkambbak.domain.survey.service;

import com.kkambbak.domain.survey.enums.DifficultyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Prioritizer 단위 테스트")
class PrioritizerTest {

    private final Prioritizer prioritizer = new Prioritizer();

    // ✅ TOPIK 순서 관련 테스트 -----------------------------------

    @Test
    @DisplayName("BEGINNER 난이도는 topik_1부터 시작하는 전체 순서를 반환한다")
    void getTopikOrder_beginner() {
        List<String> order = prioritizer.getTopikOrder(DifficultyLevel.BEGINNER);

        assertThat(order).hasSize(6);
        assertThat(order).containsExactly(
                "topik_1", "topik_2", "topik_3", "topik_4", "topik_5", "topik_6"
        );
    }

    @Test
    @DisplayName("INTERMEDIATE 난이도는 topik_3부터 시작하는 전체 순서를 반환한다")
    void getTopikOrder_intermediate() {
        List<String> order = prioritizer.getTopikOrder(DifficultyLevel.INTERMEDIATE);

        assertThat(order).hasSize(6);
        assertThat(order).containsExactly(
                "topik_3", "topik_4", "topik_5", "topik_6", "topik_1", "topik_2"
        );
    }

    @Test
    @DisplayName("ADVANCED 난이도는 topik_5부터 시작하는 전체 순서를 반환한다")
    void getTopikOrder_advanced() {
        List<String> order = prioritizer.getTopikOrder(DifficultyLevel.ADVANCED);

        assertThat(order).hasSize(6);
        assertThat(order).containsExactly(
                "topik_5", "topik_6", "topik_1", "topik_2", "topik_3", "topik_4"
        );
    }

    @Test
    @DisplayName("null 난이도 입력 시 BEGINNER 규칙을 기본으로 사용한다")
    void getTopikOrder_nullDefaultsToBeginner() {
        List<String> order = prioritizer.getTopikOrder(null);

        assertThat(order).containsExactly(
                "topik_1", "topik_2", "topik_3", "topik_4", "topik_5", "topik_6"
        );
    }

    // ✅ CASUAL 순서 관련 테스트 -----------------------------------

    @Test
    @DisplayName("daily expressions 관심사는 지정된 순서로 반환된다")
    void getCasualOrder_dailyExpressions() {
        List<String> order = prioritizer.getCasualOrder("daily expressions");

        assertThat(order).containsExactly(
                "emotions", "places", "fruits", "body", "animals", "colors"
        );
    }

    @Test
    @DisplayName("입력 문자열은 대소문자 및 공백을 무시하고 정규화된다")
    void getCasualOrder_normalizesCaseAndWhitespace() {
        List<String> order = prioritizer.getCasualOrder("  Daily   Expressions ");

        assertThat(order).containsExactly(
                "emotions", "places", "fruits", "body", "animals", "colors"
        );
    }

    @Test
    @DisplayName("정의되지 않은 관심사는 기본(daily expressions) 순서로 처리된다")
    void getCasualOrder_unknownInterestUsesDefault() {
        List<String> order = prioritizer.getCasualOrder("unknown-interest");

        assertThat(order).containsExactly(
                "emotions", "places", "fruits", "body", "animals", "colors"
        );
    }

    @Test
    @DisplayName("null 또는 빈 문자열 입력 시 기본(daily expressions) 순서로 처리된다")
    void getCasualOrder_nullOrBlankUsesDefault() {
        assertThat(prioritizer.getCasualOrder(null)).containsExactly(
                "emotions", "places", "fruits", "body", "animals", "colors"
        );

        assertThat(prioritizer.getCasualOrder("   ")).containsExactly(
                "emotions", "places", "fruits", "body", "animals", "colors"
        );
    }
}