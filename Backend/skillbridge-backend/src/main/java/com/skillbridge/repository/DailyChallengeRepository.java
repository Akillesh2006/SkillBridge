package com.skillbridge.repository;

import com.skillbridge.entity.DailyChallenge;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {
    List<DailyChallenge> findByUserOrderByCreatedAtDesc(User user);
}
