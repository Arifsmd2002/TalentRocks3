package com.project2.repository;

import com.project2.model.Subscription;
import com.project2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserAndStatus(User user, String status);

    List<Subscription> findByStatusOrderByCreatedAtDesc(String status);

    List<Subscription> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    long countByPlanAndStatus(com.project2.model.SubscriptionPlan plan, String status);

    @Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s WHERE s.status = 'ACTIVE'")
    BigDecimal sumActiveRevenue();

    @Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s WHERE s.status = 'ACTIVE' AND MONTH(s.createdAt) = MONTH(CURRENT_DATE) AND YEAR(s.createdAt) = YEAR(CURRENT_DATE)")
    BigDecimal sumMonthlyRevenue();
}
