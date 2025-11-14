package com.kkambbak.core.repository.name;

import com.kkambbak.core.entity.name.NameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NameHistoryRepository extends JpaRepository<NameHistory, Long> {
    int countByUser_Id(long userId);
}
