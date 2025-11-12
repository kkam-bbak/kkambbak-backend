package com.kkambbak.domain.roleplay.enums;

import com.kkambbak.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleplayErrorCode implements ResponseCode {
    ROLEPLAY_SCENARIO_NOT_FOUND("R001", "Roleplay Scenario not found"),
    ROLEPLAY_NO_ANSWER("R002", "Empty response from GPT model in Roleplay"),
    ROLEPLAY_GPT_ANSWER_TYPE_IS_NOT_JSON("R003", "Roleplay GPT answer type is not json"),
    ROLEPLAY_FAIL_TO_GET_ANSWER_FROM_GPT("R004","Fail to Get Answer from GPT in Roleplay"),
    UNSUPPORTED_SESSION_STATE("R005", "Unsupported Session State in Roleplay"),
    ROLEPLAY_SESSION_NOT_FOUND("R006", "Roleplay Session not found"),
    ROLEPLAY_SESSION_AND_USER_ARE_NOT_SAME("R007", "Session and User are not same"),
    ROLEPLAY_SESSION_INCONSISTENT_WITH_REQUEST("R0008","Session inconsistent with request"),
    ROLEPLAY_AUDIO_FFMEG_CONVERT_FAIL("R009","Fail to convert audio file to .wav"),
    ROLEPLAY_CREDIT_LIMIT_STANDARD("R010", "Credit limit standard"),
    ROLEPLAY_CREDIT_LIMIT_PREMIUM("R011", "Credit limit premium"),
    ROLEPLAY_TTS_UPLOAD_FAILED("R012", "TTS Upload Failed"),
    ROLEPLAY_SESSION_NO_CONTENT("R013","No Content in Session"),
    ROLEPLAY_DIALOGUE_NOT_FOUND("R014","No Dialogue Found"),
    ROLEPLAY_PRONUNCIATION_CHANCE_LIMIT("R015","Pronunciation Chance approach Limit"),
    ROLEPLAY_DIALOGUE_LIMIT("R016","Roleplay Dialogues approach Limit");

    private final String statusCode;
    private final String message;
}
