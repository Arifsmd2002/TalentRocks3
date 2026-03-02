package com.project2.model;

public enum SubscriptionPlan {
    FREE, GROWTH, PRO, ELITE;

    public int getMonthlyBids() {
        return switch (this) {
            case FREE -> 5;
            case GROWTH -> 50;
            case PRO -> 120;
            case ELITE -> Integer.MAX_VALUE; // Unlimited
        };
    }

    public double getCommissionRate() {
        return switch (this) {
            case ELITE -> 0.04;
            default -> 0.05;
        };
    }

    public int getMonthlyPrice() {
        return switch (this) {
            case FREE -> 0;
            case GROWTH -> 999;
            case PRO -> 1799;
            case ELITE -> 2499;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case FREE -> "Free";
            case GROWTH -> "Growth";
            case PRO -> "Pro";
            case ELITE -> "Elite";
        };
    }
}
