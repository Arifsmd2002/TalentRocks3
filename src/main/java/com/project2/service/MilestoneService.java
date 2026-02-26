package com.project2.service;

import com.project2.model.*;
import com.project2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final UserService userService;
    private final WalletTransactionRepository walletTransactionRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, UserService userService,
            WalletTransactionRepository walletTransactionRepository) {
        this.milestoneRepository = milestoneRepository;
        this.userService = userService;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Transactional
    public Milestone createMilestone(Milestone milestone) {
        long count = milestoneRepository.findByProjectOrderByOrderIndexAsc(milestone.getProject()).size();
        milestone.setOrderIndex((int) count + 1);
        return milestoneRepository.save(milestone);
    }

    public List<Milestone> findByProject(Project project) {
        return milestoneRepository.findByProjectOrderByOrderIndexAsc(project);
    }

    public Optional<Milestone> findById(Long id) {
        return milestoneRepository.findById(id);
    }

    @Transactional
    public void submitMilestone(Long milestoneId) {
        Milestone m = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        m.setStatus(MilestoneStatus.SUBMITTED);
        milestoneRepository.save(m);
    }

    @Transactional
    public void approveMilestone(Long milestoneId) {
        Milestone m = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        m.setStatus(MilestoneStatus.APPROVED);
        milestoneRepository.save(m);

        // Release payment: deduct 5% commission, pay freelancer
        Project project = m.getProject();
        User freelancer = project.getAssignedFreelancer();
        User client = project.getClient();

        if (m.getAmount() != null && freelancer != null && client != null) {
            // Deduct full amount from client
            userService.deductFromWallet(client, m.getAmount(), "Payment for milestone: " + m.getTitle());

            // Deduct 5% commission and pay freelancer
            java.math.BigDecimal commission = m.getAmount().multiply(new java.math.BigDecimal("0.05"));
            java.math.BigDecimal freelancerPayment = m.getAmount().subtract(commission);
            userService.addToWallet(freelancer, freelancerPayment, "Payment for milestone: " + m.getTitle());

            // Update performance score
            double currentScore = freelancer.getPerformanceScore() != null ? freelancer.getPerformanceScore() : 5.0;
            freelancer.setPerformanceScore(Math.min(10.0, currentScore + 0.1));
            userService.save(freelancer);
        }
    }

    @Transactional
    public void rejectMilestone(Long milestoneId, String feedback) {
        Milestone m = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        m.setStatus(MilestoneStatus.REJECTED);
        m.setClientFeedback(feedback);
        milestoneRepository.save(m);
    }

    @Transactional
    public Milestone save(Milestone milestone) {
        return milestoneRepository.save(milestone);
    }
}
