package com.project2.repository;

import com.project2.model.Project;
import com.project2.model.ProjectStatus;
import com.project2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByClient(User client);

    List<Project> findByAssignedFreelancer(User freelancer);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);

    List<Project> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Project p WHERE p.status = 'OPEN' AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Project> searchOpenProjects(String keyword);

    long countByStatus(ProjectStatus status);
}
