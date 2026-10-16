package com.skillbridge.repository;

import com.skillbridge.entity.CareerMission;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CareerMissionRepository extends JpaRepository<CareerMission, Long> {
    List<CareerMission> findByUserOrderByCreatedAtDesc(User user);
    Optional<CareerMission> findFirstByUserAndCompletedFalseOrderByCreatedAtDesc(User user);
    Optional<CareerMission> findFirstByUserAndCompletedTrueOrderByCompletedAtDesc(User user);
}
