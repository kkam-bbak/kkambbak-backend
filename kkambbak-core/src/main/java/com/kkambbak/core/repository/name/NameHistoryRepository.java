package com.kkambbak.core.repository.name;

import com.kkambbak.core.entity.name.NameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NameHistoryRepository extends JpaRepository<NameHistory, Long> {
    int countByUser_Id(Long userId);
    Optional<NameHistory> findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}
