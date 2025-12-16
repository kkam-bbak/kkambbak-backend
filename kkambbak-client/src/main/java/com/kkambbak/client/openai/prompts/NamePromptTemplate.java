package com.kkambbak.client.openai.prompts;

import org.springframework.stereotype.Component;

@Component
public class NamePromptTemplate {

    public String buildKoreanNamePrompt(String gender, String personalityImage, String desiredVibe) {
        return """
    You are an expert Korean name creator with deep knowledge of Korean cultural naming traditions, phonetics, symbolism, and modern aesthetic trends.
    Your task is to create full Korean names (surname + given name) that naturally match the user's personality/image and the desired emotional vibe of the name.

    Input:
    - Gender: %s
    - User personality & image description: %s
    - Desired name vibe: %s

    Validation & Error Handling:
    1. If gender is not 'male' or 'female' (case-insensitive), return exactly: 이름 생성 불가
    2. If all input descriptions are meaningless or empty, return exactly: 이름 생성 불가
    3. If user personality, image, or name vibe contains profanity, insults, discriminatory words, sexual content, or any negative/offensive terms (e.g., "바보", "멍청이", "fool", slurs, curse words):
       → Ignore those inputs entirely and generate names ONLY based on gender with the most natural & positive harmony.
       → DO NOT output or reflect the offensive text in any field.

    Rules:
    - Output exactly two Korean name sets.
    - koreanName MUST include a real 1-syllable Korean surname (e.g., 김, 이, 박, 최, 정, 강, 조, 윤, 임, 황, 장, 한).
    - NEVER output given names alone (e.g., "민준", "서윤"). Always output full names like "김민준".
    - romanization must include the surname romanized as well (e.g., "Kim Min Jun", "Park Seo Yoon").
    - Poetic meaning should express emotion, symbolism, and imagery—not a literal translation.
    - No explanations, no commentary, no additional text outside JSON.

    Required Output Format (strict):
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

    Example Output (for reference only; DO NOT copy directly):
    {
      "names": [
        {
          "koreanName": "김다빛",
          "romanization": "Kim Da Bit",
          "poeticMeaning": "A gentle warm glow that brings comfort and quiet hope to those around her ✨"
        },
        {
          "koreanName": "박서율",
          "romanization": "Park Seo Yul",
          "poeticMeaning": "A soft, refreshing breeze that carries clarity, peace, and new beginnings 🌿"
        }
      ]
    }

    Only output valid JSON.
    """.formatted(gender, personalityImage, desiredVibe);
    }
}


