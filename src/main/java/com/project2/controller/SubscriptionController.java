package com.project2.controller;

import com.project2.model.*;
import com.project2.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    // ----------------------------------------------------------------
    // STEP 1 — INITIATE PAYMENT (called from pricing modal "Continue")
    // Returns JSON with plan details for the payment UI
    // ----------------------------------------------------------------
    @GetMapping("/info/{plan}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> planInfo(@PathVariable String plan) {
        try {
            SubscriptionPlan sp = SubscriptionPlan.valueOf(plan.toUpperCase());
            Map<String, Object> data = new HashMap<>();
            data.put("plan", sp.name());
            data.put("displayName", sp.getDisplayName());
            data.put("price", sp.getMonthlyPrice());
            data.put("bids", sp.getMonthlyBids() == Integer.MAX_VALUE ? "Unlimited" : sp.getMonthlyBids());
            data.put("commission", (int) (sp.getCommissionRate() * 100) + "%");
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ----------------------------------------------------------------
    // STEP 2 — PROCESS PAYMENT (AJAX POST from payment form)
    // Simulates payment processing; activates subscription on success
    // ----------------------------------------------------------------
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> process(
            @RequestParam String plan,
            @RequestParam String paymentMethod,
            @RequestParam(required = false, defaultValue = "") String upiId,
            @RequestParam(defaultValue = "true") boolean autoRenew,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();

        if (auth == null) {
            resp.put("success", false);
            resp.put("message", "Please log in to subscribe.");
            return ResponseEntity.ok(resp);
        }

        try {
            User user = userService.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            SubscriptionPlan sp = SubscriptionPlan.valueOf(plan.toUpperCase());

            // Simulate payment gateway: always succeeds in demo
            // In production → integrate Razorpay/PayU here
            Subscription sub = subscriptionService.activate(user, sp, paymentMethod, upiId, autoRenew);

            resp.put("success", true);
            resp.put("plan", sp.getDisplayName());
            resp.put("invoice", sub.getInvoiceNumber());
            resp.put("transactionId", sub.getTransactionId());
            resp.put("startDate", sub.getStartDate().toString());
            resp.put("nextBilling", sub.getNextBillingDate().toString());
            resp.put("bids", sp.getMonthlyBids() == Integer.MAX_VALUE ? "Unlimited" : sp.getMonthlyBids());

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }

        return ResponseEntity.ok(resp);
    }

    // ----------------------------------------------------------------
    // MANAGE SUBSCRIPTION PAGE
    // ----------------------------------------------------------------
    @GetMapping("/manage")
    public String manage(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null)
            return "redirect:/login";

        Optional<Subscription> active = subscriptionService.findActive(user);
        model.addAttribute("user", user);
        model.addAttribute("subscription", active.orElse(null));
        return "subscription/manage";
    }

    // ----------------------------------------------------------------
    // CANCEL SUBSCRIPTION
    // ----------------------------------------------------------------
    @PostMapping("/cancel")
    public String cancel(Authentication auth) {
        if (auth == null)
            return "redirect:/login";
        userService.findByUsername(auth.getName())
                .ifPresent(subscriptionService::cancel);
        return "redirect:/subscription/manage";
    }
}
