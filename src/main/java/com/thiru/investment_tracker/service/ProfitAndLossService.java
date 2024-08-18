package com.thiru.investment_tracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.common.TOptional;
import com.thiru.investment_tracker.dto.ProfitAndLossContext;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.entity.FinancialReport;
import com.thiru.investment_tracker.entity.FortnightReport;
import com.thiru.investment_tracker.entity.MonthlyReport;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.RealisedProfits;
import com.thiru.investment_tracker.entity.ReportModel;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.user.UserMail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class ProfitAndLossService {

	private static final int MARCH = 3;
	private static final int DAY = 31;

	private final ProfitAndLossRepository profitAndLossRepository;

	public void updateProfitAndLoss(UserMail userMail, ProfitAndLossContext profitAndLossContext) {

		InternalContext internalContext = new InternalContext(profitAndLossContext);

		ProfitAndLossEntity profitAndLossEntity = getProfitAndLoss(userMail, profitAndLossContext);
		updateProfitAndLossReports(profitAndLossEntity, internalContext);
		profitAndLossRepository.save(profitAndLossEntity);
	}

	private ProfitAndLossEntity getProfitAndLoss(UserMail userMail, ProfitAndLossContext profitAndLossContext) {
		String email = userMail.getEmail();

		LocalDate transactionDate = profitAndLossContext.getSellContext().getTransactionDate();
		String financialYear = sanitizeFinancialYear(transactionDate);
		Optional<ProfitAndLossEntity> optionalProfitAndLoss = profitAndLossRepository.findByEmailAndFinancialYear(email,
				financialYear);

		ProfitAndLossEntity profitAndLossEntity = optionalProfitAndLoss.orElse(new ProfitAndLossEntity(email));
		if (profitAndLossEntity.getFinancialYear() == null) {
			profitAndLossEntity.setFinancialYear(financialYear);
		}
		return profitAndLossEntity;
	}

	private static void updateProfitAndLossReports(ProfitAndLossEntity profitAndLossEntity,
			InternalContext internalContext) {

		AccountType accountType = internalContext.getProfitAndLossContext().getMetadata().getAccountType();
		if (accountType == AccountType.SELF) {
			RealisedProfits existingRealisedProfits = TOptional.mapO(profitAndLossEntity.getRealisedProfits(),
					RealisedProfits.empty());
			RealisedProfits calculatedProfitDetails = calculateProfitDetails(existingRealisedProfits, internalContext);
			profitAndLossEntity.setRealisedProfits(calculatedProfitDetails);
		} else {
			RealisedProfits outSourcedRealisedProfits = TOptional
					.mapO(profitAndLossEntity.getOutSourcedRealisedProfits(), RealisedProfits.empty());
			RealisedProfits calculatedProfitDetails = calculateProfitDetails(outSourcedRealisedProfits,
					internalContext);
			profitAndLossEntity.setOutSourcedRealisedProfits(calculatedProfitDetails);
		}

		profitAndLossEntity.setLastUpdatedTime(LocalDateTime.now());
	}

	private static RealisedProfits calculateProfitDetails(RealisedProfits realisedProfits,
			InternalContext internalContext) {

		if (internalContext.isShortTermGain()) {

			FinancialReport financialReport = TOptional.mapO(realisedProfits.getShortTermCapitalGains(),
					FinancialReport.empty());
			updateFinancialReport(financialReport, internalContext);
			realisedProfits.setShortTermCapitalGains(financialReport);
		} else {

			FinancialReport financialReport = TOptional.mapO(realisedProfits.getLongTermCapitalGains(),
					FinancialReport.empty());
			updateFinancialReport(financialReport, internalContext);
			realisedProfits.setLongTermCapitalGains(financialReport);
		}

		realisedProfits.setLastUpdatedTime(LocalDateTime.now());
		return realisedProfits;
	}

	private static void updateFinancialReport(FinancialReport financialReport, InternalContext internalContext) {
		Map<Month, MonthlyReport> monthlyReports = updateMonthlyReports(financialReport.getMonthlyReport(),
				internalContext);

		financialReport.setMonthlyReport(monthlyReports);
		updateReportMetadata(financialReport, internalContext);
	}

	private static void updateReportMetadata(ReportModel metadata, InternalContext internalContext) {
		metadata.setPurchasePrice(metadata.getPurchasePrice() + internalContext.getPurchasePrice());
		metadata.setSellPrice(metadata.getSellPrice() + internalContext.getSellPrice());
		metadata.setProfit(metadata.getProfit() + internalContext.getProfit());
		metadata.setBrokerCharges(metadata.getBrokerCharges() + internalContext.getBrokerCharges());
		metadata.setMiscCharges(metadata.getMiscCharges() + internalContext.getMiscCharges());
		metadata.setLastUpdatedTime(LocalDateTime.now());
	}

	private static Map<Month, MonthlyReport> updateMonthlyReports(Map<Month, MonthlyReport> monthlyReports,
			InternalContext internalContext) {
		LocalDate transactionDate = internalContext.getProfitAndLossContext().getSellContext().getTransactionDate();
		Month month = transactionDate.getMonth();
		MonthlyReport monthlyReport = monthlyReports.getOrDefault(month, new MonthlyReport(month));

		FortnightReport fortnightReport;
		if (transactionDate.getDayOfMonth() <= 15) {
			fortnightReport = TOptional.mapO(monthlyReport.getFirstFortnightReport(), FortnightReport.from());
			monthlyReport.setFirstFortnightReport(fortnightReport);
		} else {
			fortnightReport = TOptional.mapO(monthlyReport.getSecondFortnightReport(), FortnightReport.from());
			monthlyReport.setSecondFortnightReport(fortnightReport);
		}

		updateFortnightReport(fortnightReport, internalContext);

		updateReportMetadata(monthlyReport, internalContext);
		monthlyReports.put(month, monthlyReport);

		return monthlyReports;
	}

	private static void updateFortnightReport(FortnightReport fortnightReport, InternalContext internalContext) {

		ProfitAndLossContext profitAndLossContext = internalContext.getProfitAndLossContext();

		double purchasePrice = profitAndLossContext.getPurchaseContext().getPrice()
				* profitAndLossContext.getSellContext().getQuantity();
		fortnightReport.setPurchasePrice(fortnightReport.getPurchasePrice() + purchasePrice);

		double sellPrice = profitAndLossContext.getSellContext().getPrice()
				* profitAndLossContext.getSellContext().getQuantity();
		fortnightReport.setSellPrice(fortnightReport.getSellPrice() + sellPrice);

		double purchaseBrokerCharges = profitAndLossContext.getPurchaseContext().getBrokerCharges();
		double sellBrokerCharges = profitAndLossContext.getSellContext().getBrokerCharges();
		double brokerCharges = purchaseBrokerCharges + sellBrokerCharges;
		fortnightReport.setBrokerCharges(fortnightReport.getBrokerCharges() + brokerCharges);

		double purchaseMiscCharges = profitAndLossContext.getPurchaseContext().getMiscCharges();
		double sellMiscCharges = profitAndLossContext.getSellContext().getMiscCharges();
		double miscCharges = purchaseMiscCharges + sellMiscCharges;
		fortnightReport.setMiscCharges(fortnightReport.getMiscCharges() + miscCharges);

		double netGainOrLoss = calculateGains(profitAndLossContext) - brokerCharges - miscCharges;
		fortnightReport.setProfit(fortnightReport.getProfit() + netGainOrLoss);

		// Update internal context
		internalContext.setPurchasePrice(purchasePrice);
		internalContext.setSellPrice(sellPrice);
		internalContext.setProfit(netGainOrLoss);
		internalContext.setBrokerCharges(brokerCharges);
		internalContext.setMiscCharges(miscCharges);
	}

	private static double calculateGains(ProfitAndLossContext profitAndLossContext) {

		double sellQuantity = profitAndLossContext.getSellContext().getQuantity();
		double initialPrice = profitAndLossContext.getPurchaseContext().getPrice();
		double currentPrice = profitAndLossContext.getSellContext().getPrice();

		return (currentPrice - initialPrice) * sellQuantity;
	}

	private static String sanitizeFinancialYear(LocalDate transactionDate) {

		int transactionYear = transactionDate.getYear();
		LocalDate financialYearEnd = financialYearEnd(transactionYear);

		if (transactionDate.isBefore(financialYearEnd)) {
			return (transactionYear - 1) + "-" + transactionYear;
		}

		return transactionYear + "-" + (transactionYear + 1);
	}

	private static LocalDate financialYearEnd(int year) {
		return LocalDate.of(year, MARCH, DAY);
	}

	public ProfitAndLossResponse getProfitAndLoss(UserMail userMail, String financialYear) {

		String email = userMail.getEmail();

		Optional<ProfitAndLossEntity> optionalProfitAndLoss = profitAndLossRepository.findByEmailAndFinancialYear(email,
				financialYear);
		ProfitAndLossEntity profitAndLossEntity = optionalProfitAndLoss.orElse(new ProfitAndLossEntity());

		return TObjectMapper.copy(profitAndLossEntity, ProfitAndLossResponse.class);
	}

	public void deleteProfitAndLoss(UserMail userMail) {
		profitAndLossRepository.deleteByEmail(userMail.getEmail());
	}
}

@Data
@NoArgsConstructor
class InternalContext {
	private boolean isShortTermGain;
	private double purchasePrice;
	private double sellPrice;
	private double profit;
	private double brokerCharges;
	private double miscCharges;
	private ProfitAndLossContext profitAndLossContext;

	InternalContext(ProfitAndLossContext profitAndLossContext) {
		this.profitAndLossContext = profitAndLossContext;
		isShortTermCapitalGain();
	}

	private void isShortTermCapitalGain() {

		LocalDate purchaseDate = profitAndLossContext.getPurchaseContext().getTransactionDate();
		LocalDate sellDate = profitAndLossContext.getSellContext().getTransactionDate();

		LocalDate thresholdDate = purchaseDate.plusYears(1);
		this.isShortTermGain = sellDate.isBefore(thresholdDate);
	}
}
