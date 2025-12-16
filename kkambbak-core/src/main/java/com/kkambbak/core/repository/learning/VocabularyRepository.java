package com.kkambbak.core.repository.learning;

import com.kkambbak.core.entity.learning.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
}