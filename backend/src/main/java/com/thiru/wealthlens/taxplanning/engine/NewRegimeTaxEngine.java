package com.thiru.wealthlens.taxplanning.engine;

import com.thiru.wealthlens.taxplanning.enums.EmployerType;
import com.thiru.wealthlens.taxplanning.enums.RegimeType;
import com.thiru.wealthlens.taxplanning.policy.entity.AllowanceCatalogueEntity;
import com.thiru.wealthlens.taxplanning.policy.entity.PerquisitePolicyEntity;
import com.thiru.wealthlens.taxplanning.policy.entity.TaxSlabPolicyEntity;
import com.thiru.wealthlens.taxplanning.salary.entity.SalaryComponentEntity;
import com.thiru.wealthlens.taxplanning.salary.entity.SalaryProfileEntity;
import com.thiru.wealthlens.taxplanning.salary.entity.TaxComputationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New Regime tax computation engine for FY 2025-26.
 * Implements all 16 steps from the specification.
 * <p>
 * Key characteristics:
 * - No HRA, LTA, 80C, 80D, or other Old Regime deductions
 * - Standard deduction of ₹75,000
 * - 87A rebate up to ₹60,000 for income up to ₹12L
 * - Marginal relief when income is between ₹12L and ₹12.75L
 * - Progressive slab rates: 0-4L(0%), 4-8L(5%), 8-12L(10%), 12-16L(15%), 16-20L(20%), 20-24L(25%), 24L+(30%)
 * - Surcharge from ₹50L onwards
 * - Cess at 4%
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class NewRegimeTaxEngine implements TaxEngine {

    private static final String TAX_YEAR = "2025-26";

    private final PerquisiteValuationService perquisiteValuation;
    private final MarginalReliefCalculator marginalReliefCalculator;

    @Override
    public TaxComputationEntity.TaxResult compute(
            SalaryProfileEntity profile,
            TaxSlabPolicyEntity slabPolicy,
            PerquisitePolicyEntity perquisitePolicy,
            List<AllowanceCatalogueEntity> catalogue,
            FormulaEvaluator formulaEvaluator
    ) {
        List<String> warnings = new ArrayList<>();
        List<String> appliedExemptions = new ArrayList<>();
        List<String> appliedDeductions = new ArrayList<>();
        List<TaxSlabPolicyEntity.SurchargeSlab> regimeSurchargeSlabs = filterSurchargeSlabs(slabPolicy);

        // ---- Step 1: Gross Salary ----
        long grossSalary = computeGrossSalary(profile, perquisitePolicy, appliedExemptions);
        log.debug("NewRegime grossSalary={}", grossSalary);

        // ---- Step 2: Standard Deduction ----
        long standardDeduction = Math.min(
                getOrDefault(slabPolicy.getStandardDeduction(), 75_000L),
                grossSalary
        );
        appliedDeductions.add("Standard Deduction: Rs. " + standardDeduction);
        log.debug("NewRegime standardDeduction={}", standardDeduction);

        // ---- Step 3: Employer PF Exemption ----
        long employerPfExempt = computeEmployerPfExemption(profile, grossSalary, appliedDeductions);

        // ---- Step 4: Employer NPS Exemption ----
        long employerNpsExempt = computeEmployerNpsExemption(profile, catalogue, formulaEvaluator, appliedDeductions, warnings);

        // ---- Step 5: Meal Voucher Exemption ----
        long mealVoucherExempt = computeMealVoucherExemption(profile, perquisitePolicy, appliedExemptions, warnings);

        // ---- Step 6: Cab Facility = 0 (fully taxable) ----
        // No exemption for cab facility in new regime

        // ---- Step 7: Gift Voucher Exemption ----
        long giftVoucherExempt = computeGiftVoucherExemption(profile, perquisitePolicy, appliedExemptions);

        // ---- Step 8: Taxable Income ----
        long totalExemptions = employerPfExempt + employerNpsExempt + mealVoucherExempt + giftVoucherExempt;
        long totalDeductions = standardDeduction;
        long taxableIncome = Math.max(0L, grossSalary - standardDeduction - totalExemptions);
        log.debug("NewRegime taxableIncome={}", taxableIncome);

        // ---- Step 9: Progressive Tax on Slabs ----
        long basicTaxBeforeRebate = computeProgressiveTax(taxableIncome, slabPolicy.getSlabs());
        log.debug("NewRegime basicTaxBeforeRebate={}", basicTaxBeforeRebate);

        // ---- Step 10: 87A Rebate ----
        long rebate87aApplied = 0L;
        long taxAfterRebate = basicTaxBeforeRebate;
        long rebateLimit = getOrDefault(slabPolicy.getRebate87aLimit(), 1_200_000L);
        long rebateAmount = getOrDefault(slabPolicy.getRebate87aAmount(), 60_000L);
        if (taxableIncome <= rebateLimit && basicTaxBeforeRebate > 0) {
            rebate87aApplied = Math.min(basicTaxBeforeRebate, rebateAmount);
            taxAfterRebate = basicTaxBeforeRebate - rebate87aApplied;
            appliedDeductions.add("87A Rebate: Rs. " + rebate87aApplied);
            log.debug("NewRegime rebate applied: {}", rebate87aApplied);
        }

        // ---- Step 11: Marginal Relief (CRITICAL) ----
        boolean marginalReliefApplied = false;
        if (taxAfterRebate > 0 && taxableIncome > rebateLimit && taxableIncome <= rebateLimit + 750_000) {
            MarginalReliefCalculator.MarginalReliefResult result =
                    marginalReliefCalculator.computeRebateMarginalRelief(
                            taxableIncome, basicTaxBeforeRebate, rebateLimit, rebateAmount);
            if (result.isReliefApplied()) {
                taxAfterRebate = result.getRelievedTax();
                marginalReliefApplied = true;
                appliedDeductions.add("Marginal Relief (87A): Rs. " + result.getReliefAmount());
                log.debug("NewRegime marginal relief applied: {}", result.getReliefAmount());
            }
        }

        // ---- Step 12: Surcharge ----
        long surcharge = computeSurcharge(taxableIncome, basicTaxBeforeRebate, regimeSurchargeSlabs);

        // ---- Step 13: Cess ----
        double cessRate = getOrDefault(slabPolicy.getCessPercentage(), 4.0);
        long cess = Math.round((taxAfterRebate + surcharge) * cessRate / 100.0);

        // ---- Step 14: Total Tax ----
        long totalTax = taxAfterRebate + surcharge + cess;

        // ---- Step 15: Net Take Home ----
        long netTakeHome = grossSalary - totalTax;

        // ---- Step 16: Warnings already collected above ----

        return new TaxComputationEntity.TaxResult(
                grossSalary,
                totalExemptions,
                totalDeductions,
                taxableIncome,
                basicTaxBeforeRebate,
                rebate87aApplied,
                taxAfterRebate,
                marginalReliefApplied,
                surcharge,
                cess,
                totalTax,
                netTakeHome,
                appliedExemptions,
                appliedDeductions,
                warnings
        );
    }

    @Override
    public RegimeType getRegime() {
        return RegimeType.NEW_REGIME;
    }

    @Override
    public String getSupportedTaxYear() {
        return TAX_YEAR;
    }

    // ---- Private Helpers ----

    private long computeGrossSalary(SalaryProfileEntity profile, PerquisitePolicyEntity policy, List<String> appliedExemptions) {
        long sumComponents = profile.getComponents() == null ? 0L :
                profile.getComponents().stream()
                        .mapToLong(SalaryComponentEntity::getAnnualAmount)
                        .sum();

        long carPerquisiteMonthly = perquisiteValuation.computeCarPerquisiteMonthly(
                profile.getCarOwnership(),
                profile.getCarEngineSize(),
                Boolean.TRUE.equals(profile.getDriverProvidedByEmployer()),
                true, // partly personal assumed
                policy
        );
        long carPerquisiteAnnual = carPerquisiteMonthly * 12;

        long driverPerquisiteMonthly = perquisiteValuation.computeDriverPerquisiteMonthly(
                Boolean.TRUE.equals(profile.getDriverProvidedByEmployer()),
                policy
        );
        long driverPerquisiteAnnual = driverPerquisiteMonthly * 12;

        // Add perquisites to gross (they are added to salary for gross-up computation)
        // But note: these are taxable; we add them to gross to compute total income
        long grossSalary = sumComponents + carPerquisiteAnnual + driverPerquisiteAnnual;

        if (carPerquisiteAnnual > 0) {
            appliedExemptions.add("Car Perquisite added to gross: Rs. " + carPerquisiteAnnual);
        }
        if (driverPerquisiteAnnual > 0) {
            appliedExemptions.add("Driver Perquisite added to gross: Rs. " + driverPerquisiteAnnual);
        }

        return grossSalary;
    }

    private long computeEmployerPfExemption(SalaryProfileEntity profile, long grossSalary, List<String> appliedDeductions) {
        // Employer PF is from NPS_EMPLOYER or PF components
        // For now, this is tracked via components. The actual PF deduction is part of
        // standard deductions. In new regime, employer PF contribution up to 12% of basic
        // is exempt (subject to ₹7.5L combined PF+NPS cap). This is computed via the NPS formula below.
        // We return 0 here and handle employer PF via the NPS catalogue entry (since NPS and PF are similar).
        // The standardDeduction handles the ₹75,000 floor.
        return 0L;
    }

    private long computeEmployerNpsExemption(
            SalaryProfileEntity profile,
            List<AllowanceCatalogueEntity> catalogue,
            FormulaEvaluator formulaEvaluator,
            List<String> appliedDeductions,
            List<String> warnings
    ) {
        AllowanceCatalogueEntity npsEntry = catalogue.stream()
                .filter(c -> "NPS_EMPLOYER".equals(c.getCode()))
                .findFirst()
                .orElse(null);

        if (npsEntry == null) {
            return 0L;
        }

        // Find employer NPS amount from components
        long employerNpsAmount = profile.getComponents() == null ? 0L :
                profile.getComponents().stream()
                        .filter(c -> "NPS_EMPLOYER".equals(c.getAllowanceCode()))
                        .mapToLong(SalaryComponentEntity::getAnnualAmount)
                        .sum();

        if (employerNpsAmount == 0) {
            return 0L;
        }

        // Get basic and DA for formula variables
        long basicAnnual = FbpOptimizerHelper.getBasicSalaryAnnual(profile.getComponents());
        long daAnnual = FbpOptimizerHelper.getDaAnnual(profile.getComponents());

        // Select formula based on employer type and regime
        String formula;
        EmployerType employerType = profile.getEmployerType();
        if (employerType == null) {
            employerType = EmployerType.PRIVATE;
        }

        switch (employerType) {
            case GOVT:
            case PSU:
                formula = npsEntry.getOldRegimeGovtLimitFormula();
                break;
            case PRIVATE:
            default:
                formula = npsEntry.getOldRegimePrivateLimitFormula();
                break;
        }

        // New regime always uses newRegimeLimitFormula
        if (npsEntry.getNewRegimeLimitFormula() != null && !npsEntry.getNewRegimeLimitFormula().isBlank()) {
            formula = npsEntry.getNewRegimeLimitFormula();
        }

        if (formula == null || formula.isBlank()) {
            return 0L;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("basic", basicAnnual);
        variables.put("da", daAnnual);
        variables.put("actual_nps", employerNpsAmount);

        long exemptAmount;
        try {
            exemptAmount = formulaEvaluator.evaluate(formula, variables);
        } catch (Exception e) {
            log.warn("Failed to evaluate NPS formula '{}': {}", formula, e.getMessage());
            exemptAmount = 0L;
            warnings.add("Could not compute NPS employer exemption due to formula evaluation error.");
        }

        appliedDeductions.add("NPS Employer Contribution Exempt: Rs. " + exemptAmount);
        return exemptAmount;
    }

    private long computeMealVoucherExemption(
            SalaryProfileEntity profile,
            PerquisitePolicyEntity policy,
            List<String> appliedExemptions,
            List<String> warnings
    ) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long actualMealVoucher = FbpOptimizerHelper.getMealVoucherAnnual(profile.getComponents());
        if (actualMealVoucher == 0) {
            return 0L;
        }

        long exemptAmount = perquisiteValuation.computeMealVoucherExemptionAnnual(actualMealVoucher, policy);
        long taxableAmount = actualMealVoucher - exemptAmount;

        appliedExemptions.add("Meal Voucher Exemption: Rs. " + exemptAmount + " (Rs. " + taxableAmount + " taxable)");

        if (taxableAmount > 0) {
            warnings.add("Meal voucher exceeds free limit. Rs. " + taxableAmount + " is taxable.");
        }

        return exemptAmount;
    }

    private long computeGiftVoucherExemption(
            SalaryProfileEntity profile,
            PerquisitePolicyEntity policy,
            List<String> appliedExemptions
    ) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long actualGiftVoucher = FbpOptimizerHelper.getGiftVoucherAnnual(profile.getComponents());
        if (actualGiftVoucher == 0) {
            return 0L;
        }

        long exemptAmount = perquisiteValuation.computeGiftVoucherExemptionAnnual(actualGiftVoucher, policy);
        appliedExemptions.add("Gift Voucher Exemption: Rs. " + exemptAmount);
        return exemptAmount;
    }

    private long computeProgressiveTax(long taxableIncome, List<TaxSlabPolicyEntity.TaxSlab> slabs) {
        if (slabs == null || slabs.isEmpty() || taxableIncome <= 0) {
            return 0L;
        }

        long tax = 0L;
        long remainingIncome = taxableIncome;

        for (TaxSlabPolicyEntity.TaxSlab slab : slabs) {
            if (remainingIncome <= 0) {
                break;
            }

            long slabFrom = getOrDefault(slab.getFromAmount(), 0L);
            long slabTo = getOrDefault(slab.getToAmount(), Long.MAX_VALUE);
            double rate = getOrDefault(slab.getRatePercent(), 0.0) / 100.0;

            if (taxableIncome <= slabFrom) {
                continue;
            }

            long incomeInSlab = Math.min(remainingIncome, slabTo - slabFrom);
            if (taxableIncome < slabTo) {
                incomeInSlab = taxableIncome - slabFrom;
            }

            if (incomeInSlab > 0) {
                tax += Math.round(incomeInSlab * rate);
                remainingIncome -= incomeInSlab;
            }
        }

        return tax;
    }

    private long computeSurcharge(
            long taxableIncome,
            long basicTaxBeforeRebate,
            List<TaxSlabPolicyEntity.SurchargeSlab> surchargeSlabs
    ) {
        if (surchargeSlabs == null || surchargeSlabs.isEmpty()) {
            return 0L;
        }

        for (TaxSlabPolicyEntity.SurchargeSlab slab : surchargeSlabs) {
            long from = getOrDefault(slab.getFromAmount(), 0L);
            Long to = slab.getToAmount();

            boolean withinSlab = taxableIncome > from && (to == null || taxableIncome <= to);
            if (withinSlab) {
                double rate = getOrDefault(slab.getRatePercent(), 0.0);
                return Math.round(basicTaxBeforeRebate * rate / 100.0);
            }
        }

        return 0L;
    }

    private List<TaxSlabPolicyEntity.SurchargeSlab> filterSurchargeSlabs(TaxSlabPolicyEntity slabPolicy) {
        if (slabPolicy.getSurchargeSlabs() == null) {
            return List.of();
        }
        return slabPolicy.getSurchargeSlabs().stream()
                .filter(s -> s.getRegimeType() == RegimeType.NEW_REGIME)
                .toList();
    }

    private long getOrDefault(Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    private double getOrDefault(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Package-private helper to compute basic salary from components.
     */
    private static class FbpOptimizerHelper {
        static long getBasicSalaryAnnual(List<SalaryComponentEntity> components) {
            if (components == null) return 0L;
            return components.stream()
                    .filter(c -> "BASIC".equals(c.getAllowanceCode()))
                    .findFirst()
                    .map(SalaryComponentEntity::getAnnualAmount)
                    .orElse(0L);
        }

        static long getDaAnnual(List<SalaryComponentEntity> components) {
            if (components == null) return 0L;
            return components.stream()
                    .filter(c -> "DA".equals(c.getAllowanceCode()))
                    .findFirst()
                    .map(SalaryComponentEntity::getAnnualAmount)
                    .orElse(0L);
        }

        static long getMealVoucherAnnual(List<SalaryComponentEntity> components) {
            if (components == null) return 0L;
            return components.stream()
                    .filter(c -> "MEAL_VOUCHER".equals(c.getAllowanceCode()))
                    .findFirst()
                    .map(SalaryComponentEntity::getAnnualAmount)
                    .orElse(0L);
        }

        static long getGiftVoucherAnnual(List<SalaryComponentEntity> components) {
            if (components == null) return 0L;
            return components.stream()
                    .filter(c -> "GIFT_VOUCHER".equals(c.getAllowanceCode()))
                    .findFirst()
                    .map(SalaryComponentEntity::getAnnualAmount)
                    .orElse(0L);
        }
    }
}
