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
 * Old Regime tax computation engine for FY 2025-26.
 * <p>
 * Key characteristics:
 * - Standard deduction of ₹50,000
 * - HRA exemption via HraExemptionCalculator
 * - LTA exemption (actual travel cost from components)
 * - Section 80C: max ₹1,50,000
 * - Section 80D: max ₹25,000 (self) + ₹25,000 (parent) = ₹50,000
 * - Section 80CCD(1B): max ₹50,000
 * - Section 80CCD(2): employer NPS (formula-based)
 * - Section 24(b): home loan interest max ₹2,00,000
 * - Professional tax: actual amount from profile
 * - Children education: ₹3,000/child/month
 * - Surcharge from ₹50L onwards
 * - Cess at 4%
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class OldRegimeTaxEngine implements TaxEngine {

    private static final String TAX_YEAR = "2025-26";
    private static final long OLD_REGIME_STANDARD_DEDUCTION = 50_000L;
    private static final long SECTION_80C_LIMIT = 1_50_000L;
    private static final long SECTION_80D_SELF_LIMIT = 25_000L;
    private static final long SECTION_80CCD_1B_LIMIT = 50_000L;
    private static final long SECTION_24B_LIMIT = 2_00_000L;
    private static final long CHILD_EDUCATION_MONTHLY = 3_000L;
    private static final long CHILD_HOSTEL_MONTHLY = 9_000L;
    private static final int MAX_CHILDREN_FOR_EXEMPTION = 2;

    private final HraExemptionCalculator hraExemptionCalculator;
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
        log.debug("OldRegime grossSalary={}", grossSalary);

        // ---- Step 2: Standard Deduction ----
        long standardDeduction = Math.min(OLD_REGIME_STANDARD_DEDUCTION, grossSalary);
        appliedDeductions.add("Standard Deduction: Rs. " + standardDeduction);

        // ---- Step 3: HRA Exemption ----
        long hraExempt = computeHraExemption(profile, appliedExemptions, warnings);
        log.debug("OldRegime hraExempt={}", hraExempt);

        // ---- Step 4: LTA Exemption ----
        long ltaExempt = computeLtaExemption(profile, appliedExemptions);

        // ---- Step 5: Employer PF Exemption ----
        long employerPfExempt = computeEmployerPfExemption(profile, appliedDeductions);

        // ---- Step 6: Employer NPS Exemption ----
        long employerNpsExempt = computeEmployerNpsExemption(profile, catalogue, formulaEvaluator, appliedDeductions, warnings);

        // ---- Step 7: Meal Voucher Exemption ----
        long mealVoucherExempt = computeMealVoucherExemption(profile, perquisitePolicy, appliedExemptions, warnings);

        // ---- Step 8: Gift Voucher Exemption ----
        long giftVoucherExempt = computeGiftVoucherExemption(profile, perquisitePolicy, appliedExemptions);

        // ---- Step 9: Section 80C ----
        long investment80c = getOrDefault(profile.getInvestment80c(), 0L);
        long section80c = Math.min(investment80c, SECTION_80C_LIMIT);
        appliedDeductions.add("Section 80C: Rs. " + section80c);

        // ---- Step 10: Section 80D ----
        long investment80d = getOrDefault(profile.getInvestment80d(), 0L);
        long section80d = Math.min(investment80d, SECTION_80D_SELF_LIMIT);
        appliedDeductions.add("Section 80D: Rs. " + section80d);

        // ---- Step 11: Section 80CCD(1B) ----
        long npsSelf80ccd1b = getOrDefault(profile.getNpsSelf80ccd1b(), 0L);
        long section80ccd1b = Math.min(npsSelf80ccd1b, SECTION_80CCD_1B_LIMIT);
        appliedDeductions.add("Section 80CCD(1B) NPS Self: Rs. " + section80ccd1b);

        // ---- Step 12: Section 24(b) Home Loan Interest ----
        long homeLoanInterest = getOrDefault(profile.getHomeLoanInterest(), 0L);
        long section24b = Math.min(homeLoanInterest, SECTION_24B_LIMIT);
        appliedDeductions.add("Section 24(b) Home Loan Interest: Rs. " + section24b);

        // ---- Step 13: Professional Tax ----
        long professionalTax = computeProfessionalTax(profile, appliedDeductions);

        // ---- Step 14: Children Education Exemption ----
        long childrenEducationExempt = computeChildrenEducationExemption(profile, appliedExemptions);

        // ---- Step 15: Hostel Expenditure Exemption ----
        long hostelExempt = computeHostelExemption(profile, appliedExemptions);

        // ---- Step 16: Books/Periodicals ----
        long booksExempt = computeBooksAndPeriodicals(profile, appliedExemptions);

        // ---- Step 17: Telephone/Mobile ----
        long telephoneExempt = computeTelephoneExemption(profile, appliedExemptions);

        // ---- Step 18: Uniform ----
        long uniformExempt = computeUniformExemption(profile, appliedExemptions);

        // ---- Step 19: Total Exemptions ----
        long totalExemptions = hraExempt + ltaExempt + mealVoucherExempt + giftVoucherExempt
                + childrenEducationExempt + hostelExempt + booksExempt + telephoneExempt + uniformExempt;

        // ---- Step 20: Total Deductions ----
        long totalDeductions = standardDeduction + employerPfExempt + employerNpsExempt
                + section80c + section80d + section80ccd1b + section24b
                + professionalTax;

        // ---- Step 21: Taxable Income ----
        long taxableIncome = Math.max(0L, grossSalary - totalExemptions - totalDeductions);
        log.debug("OldRegime taxableIncome={}", taxableIncome);

        // ---- Step 22: Progressive Tax on Old Regime Slabs ----
        // Slabs: 0-2.5L(0%), 2.5-5L(5%), 5-10L(20%), 10L+(30%)
        long basicTaxBeforeRebate = computeProgressiveTax(taxableIncome, slabPolicy.getSlabs());
        log.debug("OldRegime basicTaxBeforeRebate={}", basicTaxBeforeRebate);

        // ---- Step 23: 87A Rebate (old regime limit is ₹5,00,000) ----
        long rebateLimit = getOrDefault(slabPolicy.getRebate87aLimit(), 500_000L);
        long rebateAmount = getOrDefault(slabPolicy.getRebate87aAmount(), 12_500L);
        long rebate87aApplied = 0L;
        long taxAfterRebate = basicTaxBeforeRebate;
        if (taxableIncome <= rebateLimit && basicTaxBeforeRebate > 0) {
            rebate87aApplied = Math.min(basicTaxBeforeRebate, rebateAmount);
            taxAfterRebate = basicTaxBeforeRebate - rebate87aApplied;
            appliedDeductions.add("87A Rebate: Rs. " + rebate87aApplied);
        }

        // ---- Step 24: Marginal Relief (rebate — not typically needed for old regime but apply generically) ----
        boolean marginalReliefApplied = false;
        if (taxAfterRebate > 0 && taxableIncome > rebateLimit && taxableIncome <= rebateLimit + 750_000) {
            MarginalReliefCalculator.MarginalReliefResult result =
                    marginalReliefCalculator.computeRebateMarginalRelief(
                            taxableIncome, basicTaxBeforeRebate, rebateLimit, rebateAmount);
            if (result.isReliefApplied()) {
                taxAfterRebate = result.getRelievedTax();
                marginalReliefApplied = true;
                appliedDeductions.add("Marginal Relief (87A): Rs. " + result.getReliefAmount());
            }
        }

        // ---- Step 25: Surcharge ----
        long surcharge = computeSurcharge(taxableIncome, basicTaxBeforeRebate, regimeSurchargeSlabs);

        // ---- Step 26: Cess ----
        double cessRate = getOrDefault(slabPolicy.getCessPercentage(), 4.0);
        long cess = Math.round((taxAfterRebate + surcharge) * cessRate / 100.0);

        // ---- Step 27: Total Tax ----
        long totalTax = taxAfterRebate + surcharge + cess;

        // ---- Step 28: Net Take Home ----
        long netTakeHome = grossSalary - totalTax;

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
        return RegimeType.OLD_REGIME;
    }

    @Override
    public String getSupportedTaxYear() {
        return TAX_YEAR;
    }

    // ---- Private Helpers ----

    private long computeGrossSalary(SalaryProfileEntity profile, PerquisitePolicyEntity policy, List<String> appliedExemptions) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long sumComponents = profile.getComponents().stream()
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        long carPerquisiteMonthly = perquisiteValuation.computeCarPerquisiteMonthly(
                profile.getCarOwnership(),
                profile.getCarEngineSize(),
                Boolean.TRUE.equals(profile.getDriverProvidedByEmployer()),
                true,
                policy
        );
        long carPerquisiteAnnual = carPerquisiteMonthly * 12;

        long driverPerquisiteMonthly = perquisiteValuation.computeDriverPerquisiteMonthly(
                Boolean.TRUE.equals(profile.getDriverProvidedByEmployer()),
                policy
        );
        long driverPerquisiteAnnual = driverPerquisiteMonthly * 12;

        long grossSalary = sumComponents + carPerquisiteAnnual + driverPerquisiteAnnual;

        if (carPerquisiteAnnual > 0) {
            appliedExemptions.add("Car Perquisite added to gross: Rs. " + carPerquisiteAnnual);
        }
        if (driverPerquisiteAnnual > 0) {
            appliedExemptions.add("Driver Perquisite added to gross: Rs. " + driverPerquisiteAnnual);
        }

        return grossSalary;
    }

    private long computeHraExemption(
            SalaryProfileEntity profile,
            List<String> appliedExemptions,
            List<String> warnings
    ) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long actualHraAnnual = profile.getComponents().stream()
                .filter(c -> "HRA".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (actualHraAnnual == 0) {
            return 0L;
        }

        long basicSalaryAnnual = profile.getComponents().stream()
                .filter(c -> "BASIC".equals(c.getAllowanceCode()))
                .findFirst()
                .map(SalaryComponentEntity::getAnnualAmount)
                .orElse(0L);

        long monthlyRent = getOrDefault(profile.getMonthlyRentPaid(), 0L);
        long rentPaidAnnual = monthlyRent * 12;
        boolean isMetro = Boolean.TRUE.equals(profile.getIsMetroCity());
        boolean isPayingRent = Boolean.TRUE.equals(profile.getIsPayingRent());

        HraExemptionCalculator.HraResult hraResult = hraExemptionCalculator.computeHraExemption(
                actualHraAnnual, basicSalaryAnnual, rentPaidAnnual, isMetro, isPayingRent
        );

        appliedExemptions.add("HRA Exemption: Rs. " + hraResult.getExemptionAmount());
        warnings.addAll(hraResult.getWarnings());

        return hraResult.getExemptionAmount();
    }

    private long computeLtaExemption(SalaryProfileEntity profile, List<String> appliedExemptions) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long actualLta = profile.getComponents().stream()
                .filter(c -> "LTA".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (actualLta == 0) {
            return 0L;
        }

        // LTA is exempt to the extent of actual travel cost
        // Assuming the LTA component represents the actual claim within the 4-year block
        appliedExemptions.add("LTA Exemption: Rs. " + actualLta);
        return actualLta;
    }

    private long computeEmployerPfExemption(SalaryProfileEntity profile, List<String> appliedDeductions) {
        // Employer PF contribution is not separately exempt in old regime
        // It's part of the overall CTC structure. We return 0 as it depends on the
        // specific component structure.
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

        long employerNpsAmount = profile.getComponents() == null ? 0L :
                profile.getComponents().stream()
                        .filter(c -> "NPS_EMPLOYER".equals(c.getAllowanceCode()))
                        .mapToLong(SalaryComponentEntity::getAnnualAmount)
                        .sum();

        if (employerNpsAmount == 0) {
            return 0L;
        }

        long basicAnnual = getBasicAnnual(profile.getComponents());
        long daAnnual = getDaAnnual(profile.getComponents());

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

        long actualMealVoucher = profile.getComponents().stream()
                .filter(c -> "MEAL_VOUCHER".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

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

        long actualGiftVoucher = profile.getComponents().stream()
                .filter(c -> "GIFT_VOUCHER".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (actualGiftVoucher == 0) {
            return 0L;
        }

        long exemptAmount = perquisiteValuation.computeGiftVoucherExemptionAnnual(actualGiftVoucher, policy);
        appliedExemptions.add("Gift Voucher Exemption: Rs. " + exemptAmount);
        return exemptAmount;
    }

    private long computeProfessionalTax(SalaryProfileEntity profile, List<String> appliedDeductions) {
        // Professional tax is typically part of salary components
        // Amount varies by state; commonly ₹2,500/year
        // If found in components, use actual; otherwise 0
        if (profile.getComponents() == null) {
            return 0L;
        }

        long professionalTax = profile.getComponents().stream()
                .filter(c -> "PROFESSIONAL_TAX".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (professionalTax > 0) {
            appliedDeductions.add("Professional Tax: Rs. " + professionalTax);
        }
        return professionalTax;
    }

    private long computeChildrenEducationExemption(SalaryProfileEntity profile, List<String> appliedExemptions) {
        int numChildren = getOrDefault(profile.getNumberOfChildren(), 0);
        if (numChildren == 0) {
            return 0L;
        }

        int cappedChildren = Math.min(numChildren, MAX_CHILDREN_FOR_EXEMPTION);
        // ₹3,000 per child per month for 12 months
        long exemptAmount = cappedChildren * CHILD_EDUCATION_MONTHLY * 12;
        appliedExemptions.add("Children Education Exemption: Rs. " + exemptAmount + " (" + cappedChildren + " child(ren))");
        return exemptAmount;
    }

    private long computeHostelExemption(SalaryProfileEntity profile, List<String> appliedExemptions) {
        int numChildren = getOrDefault(profile.getNumberOfChildren(), 0);
        if (numChildren == 0) {
            return 0L;
        }

        int cappedChildren = Math.min(numChildren, MAX_CHILDREN_FOR_EXEMPTION);
        // ₹9,000 per child per month for 12 months
        long exemptAmount = cappedChildren * CHILD_HOSTEL_MONTHLY * 12;
        appliedExemptions.add("Hostel Expenditure Exemption: Rs. " + exemptAmount + " (" + cappedChildren + " child(ren))");
        return exemptAmount;
    }

    private long computeBooksAndPeriodicals(SalaryProfileEntity profile, List<String> appliedExemptions) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long booksAmount = profile.getComponents().stream()
                .filter(c -> "BOOKS_PERIODICALS".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (booksAmount > 0) {
            appliedExemptions.add("Books & Periodicals: Rs. " + booksAmount);
        }
        return booksAmount;
    }

    private long computeTelephoneExemption(SalaryProfileEntity profile, List<String> appliedExemptions) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long telephoneAmount = profile.getComponents().stream()
                .filter(c -> "TELEPHONE".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (telephoneAmount > 0) {
            appliedExemptions.add("Telephone/Mobile: Rs. " + telephoneAmount);
        }
        return telephoneAmount;
    }

    private long computeUniformExemption(SalaryProfileEntity profile, List<String> appliedExemptions) {
        if (profile.getComponents() == null) {
            return 0L;
        }

        long uniformAmount = profile.getComponents().stream()
                .filter(c -> "UNIFORM".equals(c.getAllowanceCode()))
                .mapToLong(SalaryComponentEntity::getAnnualAmount)
                .sum();

        if (uniformAmount > 0) {
            appliedExemptions.add("Uniform Allowance: Rs. " + uniformAmount);
        }
        return uniformAmount;
    }

    private long computeProgressiveTax(long taxableIncome, List<TaxSlabPolicyEntity.TaxSlab> slabs) {
        if (slabs == null || slabs.isEmpty() || taxableIncome <= 0) {
            return 0L;
        }

        long tax = 0L;

        for (TaxSlabPolicyEntity.TaxSlab slab : slabs) {
            long slabFrom = getOrDefault(slab.getFromAmount(), 0L);
            long slabTo = getOrDefault(slab.getToAmount(), Long.MAX_VALUE);
            double rate = getOrDefault(slab.getRatePercent(), 0.0) / 100.0;

            if (taxableIncome <= slabFrom) {
                continue;
            }

            long incomeInSlab = taxableIncome - slabFrom;
            if (incomeInSlab > slabTo - slabFrom) {
                incomeInSlab = slabTo - slabFrom;
            }

            if (incomeInSlab > 0) {
                tax += Math.round(incomeInSlab * rate);
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
                .filter(s -> s.getRegimeType() == RegimeType.OLD_REGIME)
                .toList();
    }

    private long getBasicAnnual(List<SalaryComponentEntity> components) {
        if (components == null) return 0L;
        return components.stream()
                .filter(c -> "BASIC".equals(c.getAllowanceCode()))
                .findFirst()
                .map(SalaryComponentEntity::getAnnualAmount)
                .orElse(0L);
    }

    private long getDaAnnual(List<SalaryComponentEntity> components) {
        if (components == null) return 0L;
        return components.stream()
                .filter(c -> "DA".equals(c.getAllowanceCode()))
                .findFirst()
                .map(SalaryComponentEntity::getAnnualAmount)
                .orElse(0L);
    }

    private long getOrDefault(Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    private double getOrDefault(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    private int getOrDefault(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}