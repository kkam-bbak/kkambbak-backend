package com.kkambbak.client.openai.prompts;

import org.springframework.stereotype.Component;

@Component
public class NamePromptTemplate {

    public String buildKoreanNamePrompt(String gender, String personality, String selfImage, String meaning) {
        return """
    You are an expert Korean name creator who deeply understands Korean name meanings, phonetics, and Western cultural nuance.
    You must create names that feel natural in Korean, emotionally resonant to English speakers, and symbolically meaningful.

    Input Variables:
    - gender: %s
    - personality type: %s
    - self-image: %s
    - meaning of the name: %s

    Validation & Error Handling:
    If any of the following are true, output exactly: 이름 생성 불가
    - gender is not male or female (case-insensitive)
    - personality, self-image, and meaning are all missing or empty

    Exception Handling for Direct Input:
    If personality, self-image, or meaning contain meaningless text (e.g., “afacsa”, “ffff”, “ㅑㄷㅅ”), ignore those fields entirely.
    If all three become empty or meaningless, generate names based only on gender.

    Rules:
    - Output exactly two Korean name sets.
    - koreanName MUST always include a Korean family name (surname) at the beginning.
        - Use only real Korean surnames (e.g., 김, 이, 박, 최, 정, 강, 조, 윤, 임, 황, 장, 한).
        - The surname must be exactly 1 syllable.
        - NEVER generate given name alone (e.g., "민준" or "서윤" is invalid).
        - Always output full name, e.g., "김민준", "박서윤", "이다은".
    - romanization must include surname romanization as well, e.g., "kim min joon", "park seo yoon".
    - Separate the two name sets with exactly one blank line.
    - No explanations, no intros, no commentary.
    - Poetic meaning must be emotional and evocative, not literal.

    Task:
        Generate exactly two Korean name sets.

        Your output MUST be strictly in this JSON structure:

        {
          "names": [
            {
              "koreanName": "string",
              "romanization": "string",
              "poeticMeaning": "string"
            },
            {
              "koreanName": "string",
              "romanization": "string",
              "poeticMeaning": "string"
            }
          ]
        }

    --- Example Output (For Reference Only / NOT actual format to output) ---

    Example:
        {
          "names": [
            {
              "koreanName": "김민준",
              "romanization": "kim min joon",
              "poeticMeaning": "A radiant spirit born to move forward with quiet strength 🌟"
            },
            {
              "koreanName": "박서윤",
              "romanization": "park seo yoon",
              "poeticMeaning": "Like a soft breeze carrying new hope into the morning light 🌿"
            }
          ]
        }

    Only output JSON. Do NOT output explanations or commentary.
    """.formatted(gender, personality, selfImage, meaning);
    }
}

