package com.thiru.wealthlens.shared.util.calculator;

import com.thiru.wealthlens.portfolio.dto.analytics.CashFlow;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class XirrCalculator {

    private static final double EPSILON = 1e-7;
    private static final int MAX_ITERATIONS = 100;
    private static final double INITIAL_GUESS = 0.1;

    private XirrCalculator() {}

    public static double calculate(List<CashFlow> cashFlows) {
        if (cashFlows.size() < 2) {
            throw new IllegalArgumentException("At least 2 cash flows are required for XIRR calculation");
        }

        boolean hasPositive = cashFlows.stream().anyMatch(cf -> cf.getAmount() > 0);
        boolean hasNegative = cashFlows.stream().anyMatch(cf -> cf.getAmount() < 0);
        if (!hasPositive || !hasNegative) {
            throw new IllegalArgumentException("XIRR requires at least one positive and one negative cash flow");
        }

        LocalDate baseDate = cashFlows.get(0).getDate();
        double r = INITIAL_GUESS;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double npv = npv(cashFlows, baseDate, r);
            if (Math.abs(npv) < EPSILON) {
                return r;
            }
            double derivative = npvDerivative(cashFlows, baseDate, r);
            if (Math.abs(derivative) < EPSILON) {
                return fallbackToBisection(cashFlows, baseDate);
            }
            r = r - npv / derivative;
        }

        return fallbackToBisection(cashFlows, baseDate);
    }

    private static double npv(List<CashFlow> cashFlows, LocalDate baseDate, double r) {
        return cashFlows.stream()
            .mapToDouble(cf -> cf.getAmount() / Math.pow(1 + r, daysBetween(baseDate, cf.getDate()) / 365.0))
            .sum();
    }

    private static double npvDerivative(List<CashFlow> cashFlows, LocalDate baseDate, double r) {
        return cashFlows.stream()
            .mapToDouble(cf -> {
                double days = daysBetween(baseDate, cf.getDate()) / 365.0;
                return -days * cf.getAmount() / Math.pow(1 + r, days + 1);
            })
            .sum();
    }

    private static double fallbackToBisection(List<CashFlow> cashFlows, LocalDate baseDate) {
        double low = -0.9999;
        double high = 10.0;
        double mid = 0.0;

        for (int i = 0; i < 100; i++) {
            mid = (low + high) / 2.0;
            double npvMid = npv(cashFlows, baseDate, mid);

            if (Math.abs(npvMid) < EPSILON) {
                return mid;
            }

            double npvLow = npv(cashFlows, baseDate, low);
            if (npvMid * npvLow < 0) {
                high = mid;
            } else {
                low = mid;
            }
        }

        return mid;
    }

    private static double daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static double annualizedReturn(double totalValue, double totalInvestment, double days) {
        if (totalInvestment <= 0 || days <= 0) {
            return 0.0;
        }
        return Math.pow(totalValue / totalInvestment, 365.0 / days) - 1;
    }
}
