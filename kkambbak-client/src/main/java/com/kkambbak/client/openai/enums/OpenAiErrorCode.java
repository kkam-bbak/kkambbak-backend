package com.kkambbak.client.openai.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAiErrorCode {
    OPEN_AI_TIMEOUT("OA001","OpenAI 요청이 제한 시간 내에 응답하지 않았습니다."),
    OPEN_AI_CONNECTION_ERROR("OA002", "OpenAI 서버에 연결할 수 없습니다."),
    OPEN_AI_UNAUTHORIZED_ERROR("OA003", "유효하지 않은 OpenAI API 키입니다."),
    OPEN_AI_BAD_REQUEST_ERROR("OA004", "잘못된 요청 형식입니다."),
    OPEN_AI_RATE_LIMIT_ERROR("OA005", "OpenAI 호출 한도를 초과했습니다."),
    OPEN_AI_SERVER_ERROR("OA006", "OpenAI 서버 내부 오류가 발생했습니다."),
    OPEN_AI_INVALID_RESPONSE_ERROR("OA007", "OpenAI 응답 형식이 올바르지 않습니다."),
    OPEN_AI_UNKNOWN_ERROR("OA008", "OpenAI 통신 중 알 수 없는 오류가 발생했습니다."),
    OPEN_AI_CONTEXT_EXCEEDED("OA009", "모델의 최대 컨텍스트 길이를 초과했습니다."),
    OPEN_AI_MODEL_NOT_FOUND("OA010", "요청한 OpenAI 모델을 찾을 수 없습니다."),
    OPEN_AI_QUOTA_EXCEEDED("OA011", "OpenAI 사용 한도를 초과했습니다.");


    private final String code;
    private final String message;
}
