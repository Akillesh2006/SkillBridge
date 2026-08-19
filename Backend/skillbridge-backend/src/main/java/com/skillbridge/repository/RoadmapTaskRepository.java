package com.skillbridge.repository;

import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.RoadmapTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapTaskRepository extends JpaRepository<RoadmapTask, Long> {

    List<RoadmapTask> findByRoadmapOrderByWeekNumberAsc(Roadmap roadmap);
}