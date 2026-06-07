package com.thiru.investment_tracker.service;


import com.thiru.investment_tracker.dto.FinanceRequest;
import com.thiru.investment_tracker.dto.FinanceResponse;
import com.thiru.investment_tracker.dto.enums.CalculationType;
import org.springframework.stereotype.Service;

@Service
public class FinancesService {

    public FinanceResponse getFinanceResponse(FinanceRequest financeRequest) {

        CalculationType calculationType = financeRequest.getCalculationType();
        return switch (calculationType) {
            case EMI -> calculateEmi(financeRequest);
            case TOTAL_AMOUNT -> calculateFinalAmount(financeRequest);
            case PRINCIPAL_AMOUNT -> null;
            case RATE_OF_INTEREST ->
                    FinanceResponse.builder().rateOfInterest(financeRequest.getRateOfInterest()).build();
            case TIME_PERIOD,//                return new FinanceResponse().timePeriod(financeRequest.getTimePeriod());
                 NONE -> throw new IllegalArgumentException("Invalid calculation type");
        };
    }

    private FinanceResponse calculateEmi(FinanceRequest financeRequest) {

        double emiAmount = calculatedEmi(financeRequest);
        return FinanceResponse.builder().emiAmount((int) Math.ceil(emiAmount)).build();
    }

    private FinanceResponse calculateFinalAmount(FinanceRequest financeRequest) {
        double finalAmount = calculatedEmi(financeRequest) * financeRequest.getTimePeriod();
        return FinanceResponse.builder().finalAmount(finalAmount).build();
    }

    private double calculatedEmi(FinanceRequest financeRequest) {
        double principalAmount = financeRequest.getPrincipalAmount();
        double percentageOfRateOfInterest = financeRequest.getRateOfInterest();
        double rateOfInterestPerMonth = percentageOfRateOfInterest / (12 * 100);
        int timePeriod = financeRequest.getTimePeriod();

        return principalAmount * rateOfInterestPerMonth
                * Math.pow(1 + rateOfInterestPerMonth, timePeriod) / (Math.pow(1 + rateOfInterestPerMonth, timePeriod) - 1);
    }
}
