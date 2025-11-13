package com.kkambbak.client.openai.prompts;

import org.springframework.stereotype.Component;

@Component
public class OpenAiPromptTemplate {

    /**
     * 롤플레이 시작 시 사용할 GPT 프롬프트
     */
    public String buildRoleplayStartPrompt(String scenarioTitle) {
        return """
        You are a Korean conversation teacher helping learners practice realistic two-person roleplays.
        Each roleplay involves two polite Korean speakers (A and B) appropriate for the given scenario.
        
        Scenario: %s
        
        Instructions:
        1. Write one natural and polite Korean line that fits the scenario.
        2. Write one mismatched line that is polite and grammatically correct, but contextually strange or socially awkward.
           (It should sound like something a learner might mistakenly say in this situation.)
        3. Extract one short English word that captures the *main intent* of the natural Korean sentence.
           - Use one simple everyday word such as "order", "thank", "wait", "help", "call".
           - The word should describe the *action or purpose* of the Korean sentence, not a translation.
        
        Purpose:
        Learners will compare natural vs. mismatched sentences to understand proper context,  
        and use the core English word to remember the key intent of the expression.
        
        Output format (strict JSON):
        {
          "korean": "자연스러운 한국어 문장",
          "english": "English translation of the natural sentence",
          "speaker": "role name in English (e.g. staff, customer, doctor, patient)",
          "mismatchKorean": "맥락에 어울리지 않는 한국어 문장",
          "mismatchEnglish": "English translation of the mismatch sentence",
          "coreWord": "a simple English keyword (e.g. order, thank, greet, wait)"
        }
        
        Rules:
        - Output exactly one JSON object, no markdown or explanations.
        - Both sentences must be concise and natural spoken Korean.
        - The mismatch sentence must be different from previous mismatch sentences.
        - The mismatch must still sound polite and grammatical.
        - The coreWord must be exactly one short English word, not a phrase.
        - Maintain continuity with the previous conversation if context exists.
        """.formatted(scenarioTitle);
    }

    /**
     * 롤플레이 진행 중 다음 문장 생성용 GPT 프롬프트
     */
    public String buildRoleplayNextPrompt(String prevRole) {
        return """
        Continue the Korean roleplay based on the previous conversation history and scenario.
        The next line should:
        - Be spoken naturally by the other person (not %s)
        - Match the tone, topic, and politeness level of the previous exchange
        - Keep both sentences concise and natural spoken Korean
        - Include a contextually mismatched but grammatically correct alternative
        
        Output exactly one JSON object with:
        {korean, english, speaker, mismatchKorean, mismatchEnglish, coreWord}
        No explanations or markdown.
        """.formatted(prevRole);
    }



}
