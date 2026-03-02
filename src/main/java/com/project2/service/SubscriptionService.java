package com.project2.service;

import com.project2.model.*;
import com.project2.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            WalletTransactionRepository walletTransactionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    // ----------------------------------------------------------------
    // ACTIVATE / UPGRADE SUBSCRIPTION
    // ----------------------------------------------------------------
    @Transactional
    public Subscription activate(User user, SubscriptionPlan plan,
            String paymentMethod, String upiId,
            boolean autoRenew) {

        // Cancel any existing active subscription first
        Optional<Subscription> existing = subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
        existing.ifPresent(s -> {
            s.setStatus("CANCELLED");
            subscriptionRepository.save(s);
        });

        int price = plan.getMonthlyPrice();

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan(plan);
        sub.setStatus("ACTIVE");
        sub.setPaymentMethod(paymentMethod);
        sub.setAmountPaid(BigDecimal.valueOf(price));
        sub.setUpiId(upiId);
        sub.setAutoRenew(autoRenew);
        sub.setStartDate(LocalDate.now());
        sub.setNextBillingDate(LocalDate.now().plusMonths(1));
        sub.setBidsUsedThisCycle(0);

        // Generate invoice number
        String invoice = "TR-" + LocalDate.now().getYear()
                + "-" + String.format("%06d", (long) (Math.random() * 999999));
        sub.setInvoiceNumber(invoice);

        // Generate transaction ID
        sub.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());

        subscriptionRepository.save(sub);

        // Record wallet transaction for paid plans
        if (price > 0) {
            WalletTransaction tx = new WalletTransaction(
                    user,
                    BigDecimal.valueOf(price),
                    "SUBSCRIPTION",
                    plan.getDisplayName() + " Plan Subscription — " + invoice);
            walletTransactionRepository.save(tx);
        }

        return sub;
    }

    // ----------------------------------------------------------------
    // FIND ACTIVE SUBSCRIPTION FOR USER
    // ----------------------------------------------------------------
    public Optional<Subscription> findActive(User user) {
        return subscriptionRepository.findByUserAndStatus(user, "ACTIVE");
    }

    // ----------------------------------------------------------------
    // CANCEL SUBSCRIPTION
    // ----------------------------------------------------------------
    @Transactional
    public void cancel(User user) {
        subscriptionRepository.findByUserAndStatus(user, "ACTIVE").ifPresent(s -> {
            s.setStatus("CANCELLED");
            s.setAutoRenew(false);
            subscriptionRepository.save(s);
        });
    }

    // ----------------------------------------------------------------
    // ADMIN ANALYTICS
    // ----------------------------------------------------------------
    public long countActive() {
        return subscriptionRepository.countByStatus("ACTIVE");
    }

    public long countCancelled() {
        return subscriptionRepository.countByStatus("CANCELLED");
    }

    public long countByPlan(SubscriptionPlan plan) {
        return subscriptionRepository.countByPlanAndStatus(plan, "ACTIVE");
    }

    public BigDecimal monthlyRevenue() {
        BigDecimal r = subscriptionRepository.sumMonthlyRevenue();
        return r != null ? r : BigDecimal.ZERO;
    }

    public List<Subscription> findAll() {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Subscription> findFailed() {
        return subscriptionRepository.findByStatusOrderByCreatedAtDesc("FAILED");
    }

    // ----------------------------------------------------------------
    // DOWNGRADE TO FREE (grace period expired)
    // ----------------------------------------------------------------
    @Transactional
    public void downgradeToFree(User user) {
        cancel(user);
        // Free plan requires no Subscription record — bid limits reset via plan enum
    }
}
