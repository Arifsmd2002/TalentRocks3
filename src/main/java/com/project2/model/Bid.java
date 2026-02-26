package com.project2.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal bidAmount;

    private Integer deliveryDays;

    @Column(columnDefinition = "TEXT")
    private String proposal;

    @Column(columnDefinition = "TEXT")
    private String demoWorkUrl;

    private String demoFilePath;

    @Enumerated(EnumType.STRING)
    private BidStatus status = BidStatus.PENDING;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    public Bid() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getFreelancer() {
        return freelancer;
    }

    public void setFreelancer(User freelancer) {
        this.freelancer = freelancer;
    }

    public java.math.BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(java.math.BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getProposal() {
        return proposal;
    }

    public void setProposal(String proposal) {
        this.proposal = proposal;
    }

    public String getDemoWorkUrl() {
        return demoWorkUrl;
    }

    public void setDemoWorkUrl(String demoWorkUrl) {
        this.demoWorkUrl = demoWorkUrl;
    }

    public String getDemoFilePath() {
        return demoFilePath;
    }

    public void setDemoFilePath(String demoFilePath) {
        this.demoFilePath = demoFilePath;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
