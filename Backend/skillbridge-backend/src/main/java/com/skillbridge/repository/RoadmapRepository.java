package com.skillbridge.repository;

import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findByUserAndRoleName(User user, String roleName);

    List<Roadmap> findByUser(User user);
}