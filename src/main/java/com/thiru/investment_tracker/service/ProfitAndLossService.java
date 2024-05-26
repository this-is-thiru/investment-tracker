package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.ProfitAndLossContext;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import com.thiru.investment_tracker.entity.RealisedProfits;
import com.thiru.investment_tracker.repository.ProfitAndLossRepository;
import com.thiru.investment_tracker.user.UserMail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class ProfitAndLossService {

    private static final int MARCH = Calendar.MARCH;
    private static final int DAY = 31;

    private final ProfitAndLossRepository profitAndLossRepository;

    public void updateProfitAndLoss(UserMail userMail, ProfitAndLossContext profitAndLossContext) {

        String email = userMail.getEmail();
        String financialYear = sanitizeFinancialYear(profitAndLossContext.getSellDate());
        Optional<ProfitAndLossEntity> optionalProfitAndLoss =
                profitAndLossRepository.findByEmailAndFinancialYear(email, financialYear);

        ProfitAndLossEntity profitAndLossEntity = optionalProfitAndLoss.orElse(new ProfitAndLossEntity());
        if (profitAndLossEntity.getFinancialYear() == null) {
            profitAndLossEntity.setFinancialYear(financialYear);
        }
        updateCapitalGains(profitAndLossContext, profitAndLossEntity);

        profitAndLossRepository.save(profitAndLossEntity);
    }

    private static void updateCapitalGains(ProfitAndLossContext profitAndLossContext, ProfitAndLossEntity profitAndLossEntity) {

        RealisedProfits existingRealisedProfits = profitAndLossEntity.getRealisedProfits();

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

        return  (currentPrice - initialPrice) * sellQuantity;
    }

    private static boolean isShortTermCapitalGain(ProfitAndLossContext profitAndLossContext) {
        Calendar purchaseDate = toCalendar(profitAndLossContext.getPurchaseDate());
        purchaseDate.roll(Calendar.YEAR, true);
        Calendar sellDate = toCalendar(profitAndLossContext.getSellDate());

        return sellDate.before(purchaseDate);
    }

    private static String sanitizeFinancialYear(Date transactionMadeOn) {

        Calendar transactionDate = toCalendar(transactionMadeOn);
        int transactionYear = transactionDate.get(Calendar.YEAR);
        Calendar financialYearEnd = toCalendar(transactionYear);

        if (transactionDate.before(financialYearEnd)) {
            return (transactionYear - 1) + "/" + transactionYear;
        }

        return transactionYear + "/" + (transactionYear + 1);
    }

    private static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private static Calendar toCalendar(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, MARCH, DAY);
        return cal;
    }

    public ProfitAndLossResponse getProfitAndLoss(UserMail userMail, String financialYear) {

        String email = userMail.getEmail();

        Optional<ProfitAndLossEntity> optionalProfitAndLoss =
                profitAndLossRepository.findByEmailAndFinancialYear(email, financialYear);
        ProfitAndLossEntity profitAndLossEntity = optionalProfitAndLoss.orElse(new ProfitAndLossEntity());

        return CommonUtil.copy(profitAndLossEntity, ProfitAndLossResponse.class);
    }
}
