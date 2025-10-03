package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.context.BrokerChargeContext;
import com.thiru.investment_tracker.dto.enums.AmcChargeFrequency;
import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.BrokerageAggregatorType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.BrokerCharges;
import com.thiru.investment_tracker.entity.UserBrokerCharges;
import com.thiru.investment_tracker.entity.model.BrokerageCharges;
import com.thiru.investment_tracker.repository.UserBrokerChargesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBrokerChargeService {

    private final UserBrokerChargesRepository userBrokerChargesRepository;
    private final BrokerChargeService brokerChargeService;

    public UserBrokerCharges addUserBrokerChargeEntry(UserMail userMail, BrokerChargeContext brokerChargeContext) {
        BrokerName brokerName = brokerChargeContext.brokerName();
        String stockCode = brokerChargeContext.stockCode();
        LocalDate transactionDate = brokerChargeContext.transactionDate();

        BrokerCharges brokerCharges = brokerChargeService.getBrokerCharge(brokerName, transactionDate);
        if (brokerCharges == null) {
            log.error("Broker charges not found for user: {}, brokerContext: {}", userMail, brokerChargeContext);
            return null;
        }

        UserBrokerCharges brokerCharge = getUserBrokerCharges(userMail, brokerChargeContext, brokerCharges);
        var brokerChargesOptional = userBrokerChargesRepository.findTopSellTxnByBrokerNameAndStockCodeAndTransactionDate(userMail.getEmail(), brokerName, stockCode, transactionDate);
        if (brokerChargesOptional.isEmpty()) {
            brokerCharge.setDpCharges(brokerCharge.getDpCharges());
        }
        return userBrokerChargesRepository.save(brokerCharge);
    }

    public void deleteUserBrokerCharges(UserMail userMail) {
        userBrokerChargesRepository.deleteByEmail(userMail.getEmail());
    }

    private UserBrokerCharges getUserBrokerCharges(UserMail userMail, BrokerChargeContext brokerChargeContext, BrokerCharges brokerCharges) {
        UserBrokerCharges userBrokerCharges = new UserBrokerCharges();

        double dpCharges = getDpCharges(userMail, brokerChargeContext.brokerName(), brokerChargeContext.stockCode(), brokerChargeContext.transactionDate(), brokerCharges);
        userBrokerCharges.setEmail(userMail.getEmail());
        userBrokerCharges.setBrokerName(brokerChargeContext.brokerName());
        userBrokerCharges.setStockCode(brokerChargeContext.stockCode());
        userBrokerCharges.setTransactionDate(brokerChargeContext.transactionDate());
        userBrokerCharges.setType(brokerChargeContext.transactionType());
        userBrokerCharges.setBrokerage(getBrokerage(brokerChargeContext, brokerCharges.getBrokerageCharges()));
        userBrokerCharges.setGovtCharges(getGovtCharges(brokerChargeContext, brokerCharges));
        userBrokerCharges.setDpCharges(dpCharges);
        userBrokerCharges.setTransactionId(brokerChargeContext.transactionId());

        // handle amc or account opening charges
        setIfAmcOrAccountOpeningCharges(userBrokerCharges, brokerCharges, brokerChargeContext.transactionType());
        setTaxes(userBrokerCharges, brokerCharges);
        return userBrokerCharges;
    }

    private double getGovtCharges(BrokerChargeContext brokerChargeContext, BrokerCharges brokerCharges) {
        double sttCharge = brokerChargeContext.totalAmount() * brokerCharges.getStt() / 100;
        double sebiCharge = brokerChargeContext.totalAmount() * brokerCharges.getSebiCharges() / 100;
        double govtCharges = sttCharge + sebiCharge;

        var brokerChargesOptional = brokerChargeContext.transactionType();
        if (brokerChargesOptional == BrokerChargeTransactionType.BUY) {
            double stampDuty = brokerChargeContext.totalAmount() * brokerCharges.getStampDuty() / 100;
            govtCharges += stampDuty;
        }
        return govtCharges;
    }

    private static void setTaxes(UserBrokerCharges userBrokerCharges, BrokerCharges brokerCharges) {
        String[] taxComponents = brokerCharges.getGstApplicableDescription().split(",");
        double taxes = 0;
        for (String taxComponent : taxComponents) {
            taxes += getTaxComponentTax(userBrokerCharges, taxComponent);
        }
        userBrokerCharges.setTaxes(taxes);
    }

    private static double getTaxComponentTax(UserBrokerCharges userBrokerCharges, String taxComponent) {
        String[] taxComponents = taxComponent.split("-");
        double taxPercentage = Double.parseDouble(taxComponents[0]);
        String taxComponentName = taxComponents[1];
        if ("brokerage".equalsIgnoreCase(taxComponentName)) {
            return userBrokerCharges.getBrokerage() * taxPercentage / 100;
        } else if ("dp_charges".equalsIgnoreCase(taxComponentName)) {
            return userBrokerCharges.getDpCharges() * taxPercentage / 100;
        } else if ("stt".equalsIgnoreCase(taxComponentName)) {
            return userBrokerCharges.getGovtCharges() * taxPercentage / 100;
        } else if ("amc_charges".equalsIgnoreCase(taxComponentName)) {
            return userBrokerCharges.getAmcCharges();
        }

        log.error("Unknown tax component: {}", taxComponentName);
        return 0.0;
    }

    private double getDpCharges(UserMail userMail, BrokerName brokerName, String stockCode, LocalDate transactionDate, BrokerCharges brokerCharges) {
        var brokerChargesOptional = userBrokerChargesRepository.findTopSellTxnByBrokerNameAndStockCodeAndTransactionDate(userMail.getEmail(), brokerName, stockCode, transactionDate);
        if (brokerChargesOptional.isEmpty()) {
            return brokerCharges.getDpChargesPerScrip();
        }
        return 0.0;
    }

    private static void setIfAmcOrAccountOpeningCharges(UserBrokerCharges userBrokerCharges, BrokerCharges brokerCharges, BrokerChargeTransactionType type) {
        if (type == BrokerChargeTransactionType.AMC_CHARGES) {
            AmcChargeFrequency amcChargeFrequency = brokerCharges.getAmcChargeFrequency();
            if (amcChargeFrequency == AmcChargeFrequency.ANNUALLY) {
                userBrokerCharges.setAmcCharges(brokerCharges.getAmcChargesAnnually());
            } else if (amcChargeFrequency == AmcChargeFrequency.QUARTERLY) {
                double amcCharges = brokerCharges.getAmcChargesAnnually() / 4;
                userBrokerCharges.setAmcCharges(amcCharges);
            } else {
                log.error("Invalid amc charge frequency: {}", amcChargeFrequency);
            }
        } else if (type == BrokerChargeTransactionType.ACCOUNT_OPENING_CHARGES) {
            userBrokerCharges.setAccountOpeningCharges(brokerCharges.getAccountOpeningCharges());
        }
    }

    private static double getBrokerage(BrokerChargeContext brokerChargeContext, BrokerageCharges brokerageCharges) {

        if (brokerageCharges == null) {
            return 0;
        }

        double brokeragePercentage = brokerageCharges.getBrokerage();
        double brokerageWithFromPercentage = brokerChargeContext.totalAmount() * brokeragePercentage / 100;
        double brokerageChargesAmount = brokerageCharges.getBrokerageCharges();

        if (brokerageCharges.getBrokerageAggregator() == BrokerageAggregatorType.MIN) {
            double tempBrokerage = Math.max(brokerageCharges.getMinimumBrokerage(), brokerageWithFromPercentage);
            return Math.min(tempBrokerage, brokerageChargesAmount);
        } else if (brokerageCharges.getBrokerageAggregator() == BrokerageAggregatorType.MAX) {
            double tempBrokerage = Math.min(brokerageCharges.getMaximumBrokerage(), brokerageWithFromPercentage);
            return Math.max(tempBrokerage, brokerageChargesAmount);
        }
        return 0;
    }

//
//    /**
//     * Updates the profit and loss aggregates for the given user and the financial year
//     * inferred from the sell transaction date contained in the provided context.
//     * <p>
//     * Processing flow:
//     * <ul>
//     *   <li>Derives the financial year from the sell transaction date in {@link ProfitAndLossContext}.</li>
//     *   <li>Loads (or creates) the {@link com.thiru.investment_tracker.entity.ProfitAndLossEntity} for the user and year
//     *       via {@link #getProfitAndLoss(UserMail, ProfitAndLossContext)}.</li>
//     *   <li>Updates realized profit figures based on {@link com.thiru.investment_tracker.dto.enums.AccountType}
//     *       using {@link #updateProfitAndLossReports(com.thiru.investment_tracker.entity.ProfitAndLossEntity, ProfitAndLossService.InternalContext)}.</li>
//     *   <li>Persists the updated entity using {@link #profitAndLossRepository}.</li>
//     * </ul>
//     * </p>
//     *
//     * <p>
//     * The {@code sellTransactionId} parameter allows callers to correlate this update with a specific
//     * sell transaction for auditing, tracing, or idempotency at higher layers. This method does not
//     * persist or otherwise use the identifier internally.
//     * </p>
//     *
//     * @param userMail the user identity containing the email used to partition P&amp;L documents; must not be null
//     * @param profitAndLossContext the purchase/sell context used to compute realized profit; must not be null
//     * @param sellTransactionId optional identifier of the sell transaction for traceability; must not be null
//     */
//    public void updateProfitAndLoss(UserMail userMail, ProfitAndLossContext profitAndLossContext, String sellTransactionId) {
//
//        ProfitAndLossService.InternalContext internalContext = new ProfitAndLossService.InternalContext(profitAndLossContext);
//
//        ProfitAndLossEntity profitAndLossEntity = getProfitAndLoss(userMail, profitAndLossContext);
//        updateProfitAndLossReports1(profitAndLossEntity, internalContext);
//        profitAndLossRepository.save(profitAndLossEntity);
//    }
//
//    private static void updateProfitAndLossReports1(ProfitAndLossEntity profitAndLossEntity, ProfitAndLossService.InternalContext internalContext) {
//
//        AccountType accountType = internalContext.getProfitAndLossContext().getMetadata().getAccountType();
//        if (accountType == AccountType.SELF) {
//            RealisedProfits existingRealisedProfits = TOptional.mapO(profitAndLossEntity.getRealisedProfits(), RealisedProfits.empty());
//            RealisedProfits calculatedProfitDetails = calculateProfitDetails1(existingRealisedProfits, internalContext);
//            profitAndLossEntity.setRealisedProfits(calculatedProfitDetails);
//        } else {
//            RealisedProfits outSourcedRealisedProfits = TOptional.mapO(profitAndLossEntity.getOutSourcedRealisedProfits(), RealisedProfits.empty());
//            RealisedProfits calculatedProfitDetails = calculateProfitDetails1(outSourcedRealisedProfits, internalContext);
//            profitAndLossEntity.setOutSourcedRealisedProfits(calculatedProfitDetails);
//        }
//
//        profitAndLossEntity.setLastUpdatedTime(LocalDateTime.now());
//    }
//
//    private static RealisedProfits calculateProfitDetails1(RealisedProfits realisedProfits,
//                                                           ProfitAndLossService.InternalContext internalContext) {
//
//        if (internalContext.isShortTermGain()) {
//            FinancialReport financialReport = TOptional.mapO(realisedProfits.getShortTermCapitalGains(), FinancialReport.empty());
//            updateFinancialReport1(financialReport, internalContext);
//            realisedProfits.setShortTermCapitalGains(financialReport);
//        } else {
//            FinancialReport financialReport = TOptional.mapO(realisedProfits.getLongTermCapitalGains(), FinancialReport.empty());
//            updateFinancialReport1(financialReport, internalContext);
//            realisedProfits.setLongTermCapitalGains(financialReport);
//        }
//
//        realisedProfits.setLastUpdatedTime(LocalDateTime.now());
//        return realisedProfits;
//    }
//
//    private static void updateFinancialReport1(FinancialReport financialReport, ProfitAndLossService.InternalContext internalContext) {
//        Map<Month, MonthlyReport> monthlyReports = updateMonthlyReports1(financialReport.getMonthlyReport(),
//                internalContext);
//
//        financialReport.setMonthlyReport(monthlyReports);
//        updateReportMetadata1(financialReport, internalContext);
//    }
//
//    private static void updateReportMetadata1(ReportModel metadata, ProfitAndLossService.InternalContext internalContext) {
//        metadata.setPurchasePrice(metadata.getPurchasePrice() + internalContext.getPurchasePrice());
//        metadata.setSellPrice(metadata.getSellPrice() + internalContext.getSellPrice());
//        metadata.setProfit(metadata.getProfit() + internalContext.getProfit());
//        metadata.setBrokerCharges(metadata.getBrokerCharges() + internalContext.getBrokerCharges());
//        metadata.setMiscCharges(metadata.getMiscCharges() + internalContext.getMiscCharges());
//        metadata.setLastUpdatedTime(LocalDateTime.now());
//    }
//
//    private static Map<Month, MonthlyReport> updateMonthlyReports1(Map<Month, MonthlyReport> monthlyReports,
//                                                                   ProfitAndLossService.InternalContext internalContext) {
//        LocalDate transactionDate = internalContext.getProfitAndLossContext().getSellContext().getTransactionDate();
//        Month month = transactionDate.getMonth();
//        MonthlyReport monthlyReport = monthlyReports.getOrDefault(month, new MonthlyReport(month));
//
//        FortnightReport fortnightReport;
//        if (transactionDate.getDayOfMonth() <= 15) {
//            fortnightReport = TOptional.mapO(monthlyReport.getFirstFortnightReport(), FortnightReport.from());
//            monthlyReport.setFirstFortnightReport(fortnightReport);
//        } else {
//            fortnightReport = TOptional.mapO(monthlyReport.getSecondFortnightReport(), FortnightReport.from());
//            monthlyReport.setSecondFortnightReport(fortnightReport);
//        }
//
//        updateFortnightReport(fortnightReport, internalContext);
//
//        updateReportMetadata(monthlyReport, internalContext);
//        monthlyReports.put(month, monthlyReport);
//
//        return monthlyReports;
//    }

}
