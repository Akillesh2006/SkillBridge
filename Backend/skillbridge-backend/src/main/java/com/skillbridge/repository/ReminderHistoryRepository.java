package com.skillbridge.repository;

import com.skillbridge.entity.ReminderHistory;
import com.skillbridge.entity.RoadmapTask;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReminderHistoryRepository extends JpaRepository<ReminderHistory, Long> {
    Optional<ReminderHistory> findByUserAndTaskAndReminderDate(User user, RoadmapTask task, LocalDate reminderDate);
}
