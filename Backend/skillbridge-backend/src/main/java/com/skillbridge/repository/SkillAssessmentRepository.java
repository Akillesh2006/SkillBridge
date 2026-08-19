package com.skillbridge.repository;

import com.skillbridge.entity.SkillAssessment;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillAssessmentRepository extends JpaRepository<SkillAssessment, Long> {
    List<SkillAssessment> findByUserOrderByCreatedDateDesc(User user);
    List<SkillAssessment> findByUserAndAssessmentTypeOrderByCreatedDateDesc(User user, String assessmentType);
    java.util.Optional<SkillAssessment> findFirstByUserAndRoadmapTaskOrderByCreatedDateDesc(User user, com.skillbridge.entity.RoadmapTask roadmapTask);
}
