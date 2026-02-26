package com.project2.service;

import com.project2.model.*;
import com.project2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final BidRepository bidRepository;

    public ProjectService(ProjectRepository projectRepository, BidRepository bidRepository) {
        this.projectRepository = projectRepository;
        this.bidRepository = bidRepository;
    }

    @Transactional
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Project> findAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Project> findOpenProjects() {
        return projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.OPEN);
    }

    public List<Project> findByClient(User client) {
        return projectRepository.findByClient(client);
    }

    public List<Project> findByFreelancer(User freelancer) {
        return projectRepository.findByAssignedFreelancer(freelancer);
    }

    public List<Project> searchProjects(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findOpenProjects();
        }
        return projectRepository.searchOpenProjects(keyword);
    }

    @Transactional
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public void assignFreelancer(Long projectId, User freelancer) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setAssignedFreelancer(freelancer);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        // Reject other bids
        List<Bid> otherBids = bidRepository.findByProject(project);
        otherBids.forEach(bid -> {
            if (!bid.getFreelancer().getId().equals(freelancer.getId())) {
                bid.setStatus(BidStatus.REJECTED);
                bidRepository.save(bid);
            } else {
                bid.setStatus(BidStatus.ACCEPTED);
                bidRepository.save(bid);
            }
        });
    }

    @Transactional
    public void completeProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);

            User freelancer = project.getAssignedFreelancer();
            if (freelancer != null) {
                freelancer.setCompletedProjects(
                        (freelancer.getCompletedProjects() != null ? freelancer.getCompletedProjects() : 0) + 1);
                double currentScore = freelancer.getPerformanceScore() != null ? freelancer.getPerformanceScore() : 5.0;
                freelancer.setPerformanceScore(Math.min(10.0, currentScore + 0.5)); // Bonus for completion
                // The freelancer instance is attached to the same transaction session, but
                // we'll call userService save just in case
                // Actually, projectRepository.save works, but let's be explicit if needed.
                // Since individual entities might not be saved on Repo calls for other entities
                // without Cascade,
                // we should ensure freelancer is saved.
            }
        }
    }

    public long countOpenProjects() {
        return projectRepository.countByStatus(ProjectStatus.OPEN);
    }

    public long countTotalProjects() {
        return projectRepository.count();
    }
}
