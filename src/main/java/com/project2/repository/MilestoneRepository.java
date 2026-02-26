package com.project2.repository;

import com.project2.model.Milestone;
import com.project2.model.MilestoneStatus;
import com.project2.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProjectOrderByOrderIndexAsc(Project project);

    List<Milestone> findByProjectAndStatus(Project project, MilestoneStatus status);

    long countByProjectAndStatus(Project project, MilestoneStatus status);
}
