package com.thiru.investment_tracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.common.TOptional;
import com.thiru.investment_tracker.dto.ProfitAndLossContext;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.RealisedProfits;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.user.UserMail;

import lombok.AllArgsConstructor;
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

		String email = userMail.getEmail();
		String financialYear = sanitizeFinancialYear(profitAndLossContext.getSellDate());
		Optional<ProfitAndLossEntity> optionalProfitAndLoss = profitAndLossRepository.findByEmailAndFinancialYear(email,
				financialYear);

		ProfitAndLossEntity profitAndLossEntity = optionalProfitAndLoss.orElse(new ProfitAndLossEntity(email));
		if (profitAndLossEntity.getFinancialYear() == null) {
			profitAndLossEntity.setFinancialYear(financialYear);
		}
		updateCapitalGains(profitAndLossContext, profitAndLossEntity);

		profitAndLossRepository.save(profitAndLossEntity);
	}

	private static void updateCapitalGains(ProfitAndLossContext profitAndLossContext,
			ProfitAndLossEntity profitAndLossEntity) {

		if (profitAndLossContext.getAccountType() == AccountType.SELF) {
			RealisedProfits existingRealisedProfits = TOptional.mapO(profitAndLossEntity.getRealisedProfits(),
					RealisedProfits.empty());
			RealisedProfits calculatedProfitDetails = calculateProfitDetails(profitAndLossContext,
					existingRealisedProfits);
			profitAndLossEntity.setRealisedProfits(calculatedProfitDetails);
		} else {
			RealisedProfits outSourcedRealisedProfits = TOptional
					.mapO(profitAndLossEntity.getOutSourcedRealisedProfits(), RealisedProfits.empty());
			RealisedProfits calculatedProfitDetails = calculateProfitDetails(profitAndLossContext,
					outSourcedRealisedProfits);
			profitAndLossEntity.setOutSourcedRealisedProfits(calculatedProfitDetails);
		}

		double totalRealisedProfit = totalProfitDetails(profitAndLossEntity);
		boolean isProfit = totalRealisedProfit > 0;
		profitAndLossEntity.setProfit(isProfit);

		profitAndLossEntity.setLastUpdatedTime(LocalDateTime.now());
	}

	private static double totalProfitDetails(ProfitAndLossEntity profitAndLossEntity) {

		RealisedProfits realisedProfits = TOptional.mapO(profitAndLossEntity.getRealisedProfits(),
				RealisedProfits.empty());
		RealisedProfits outSourcedRealisedProfits = TOptional.mapO(profitAndLossEntity.getOutSourcedRealisedProfits(),
				RealisedProfits.empty());

		double shortTermCapitalGains = realisedProfits.getShortTermCapitalGains();
		double longTermCapitalGains = realisedProfits.getLongTermCapitalGains();

		double outSourcedShortTermCapitalGains = outSourcedRealisedProfits.getShortTermCapitalGains();
		double outSourcedLongTermCapitalGains = outSourcedRealisedProfits.getLongTermCapitalGains();

		return shortTermCapitalGains + longTermCapitalGains + outSourcedShortTermCapitalGains
				+ outSourcedLongTermCapitalGains;
	}

	private static RealisedProfits calculateProfitDetails(ProfitAndLossContext profitAndLossContext,
			RealisedProfits realisedProfits) {

		double calculatedGainOrLoss = calculateGains(profitAndLossContext);

		if (isShortTermCapitalGain(profitAndLossContext)) {
			double shortTermCapitalGains = realisedProfits.getShortTermCapitalGains();
			shortTermCapitalGains = shortTermCapitalGains + calculatedGainOrLoss;
			realisedProfits.setShortTermCapitalGains(shortTermCapitalGains);
		} else {
			double longTermCapitalGains = realisedProfits.getLongTermCapitalGains();
			longTermCapitalGains = longTermCapitalGains + calculatedGainOrLoss;
			realisedProfits.setLongTermCapitalGains(longTermCapitalGains);
		}

		double totalRealisedProfit = realisedProfits.getTotalRealisedProfit();
		totalRealisedProfit = totalRealisedProfit + calculatedGainOrLoss;
		realisedProfits.setTotalRealisedProfit(totalRealisedProfit);

		return realisedProfits;
	}

	private static double calculateGains(ProfitAndLossContext profitAndLossContext) {

		long sellQuantity = profitAndLossContext.getSellQuantity();
		double initialPrice = profitAndLossContext.getPurchasePrice();
		double currentPrice = profitAndLossContext.getSellPrice();

		return (currentPrice - initialPrice) * sellQuantity;
	}

	private static boolean isShortTermCapitalGain(ProfitAndLossContext profitAndLossContext) {

		LocalDate purchaseDate = profitAndLossContext.getPurchaseDate();
		LocalDate sellDate = profitAndLossContext.getSellDate();

		LocalDate thresholdDate = purchaseDate.plusYears(1);

		return sellDate.isBefore(thresholdDate);
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
