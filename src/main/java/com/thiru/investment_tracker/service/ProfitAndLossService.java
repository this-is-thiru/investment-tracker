package com.thiru.investment_tracker.service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiru.investment_tracker.dto.ProfitAndLossContext;
import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.common.TOptional;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
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

	private static final int C_MARCH = Calendar.MARCH;
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

		RealisedProfits existingRealisedProfits = TOptional.mapO(profitAndLossEntity.getRealisedProfits(),
				RealisedProfits.empty());

		double calculatedGainOrLoss = calculateGains(profitAndLossContext);

		if (isShortTermCapitalGain(profitAndLossContext)) {
			double shortTermCapitalGains = existingRealisedProfits.getShortTermCapitalGains();
			shortTermCapitalGains = shortTermCapitalGains + calculatedGainOrLoss;
			existingRealisedProfits.setShortTermCapitalGains(shortTermCapitalGains);
		} else {
			double longTermCapitalGains = existingRealisedProfits.getLongTermCapitalGains();
			longTermCapitalGains = longTermCapitalGains + calculatedGainOrLoss;
			existingRealisedProfits.setLongTermCapitalGains(longTermCapitalGains);
		}

		double totalRealisedProfit = existingRealisedProfits.getTotalRealisedProfit();
		totalRealisedProfit = totalRealisedProfit + calculatedGainOrLoss;
		existingRealisedProfits.setTotalRealisedProfit(totalRealisedProfit);

		boolean isProfit = totalRealisedProfit > 0;
		profitAndLossEntity.setProfit(isProfit);
		profitAndLossEntity.setRealisedProfits(existingRealisedProfits);
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

	private static Calendar toCalendar(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, C_MARCH, DAY);
		return cal;
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
}
