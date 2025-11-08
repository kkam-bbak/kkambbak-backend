package com.kkambbak.core.repository.survey;

import com.kkambbak.core.entity.survey.Survey;
import com.kkambbak.core.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    // 해당 유저의 설문이 이미 존재하는지 여부 확인
    boolean existsByUser_Id(Long userId);

    // 유저의 설문 조회
    Optional<Survey> findByUser_Id(Long userId);

}
