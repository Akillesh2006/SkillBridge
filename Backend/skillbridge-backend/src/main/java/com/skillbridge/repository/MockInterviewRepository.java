package com.skillbridge.repository;

import com.skillbridge.entity.MockInterview;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterview, Long> {
    List<MockInterview> findByUserOrderByCreatedAtDesc(User user);
}
