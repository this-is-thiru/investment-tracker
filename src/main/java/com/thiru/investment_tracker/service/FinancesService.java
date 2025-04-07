package com.thiru.investment_tracker.service;


import com.thiru.investment_tracker.model.CalculationType;
import com.thiru.investment_tracker.model.FinanceRequest;
import com.thiru.investment_tracker.model.FinanceResponse;
import org.springframework.stereotype.Service;

@Service
public class FinancesService {

    public FinanceResponse getFinanceResponse(FinanceRequest financeRequest) {

        CalculationType calculationType = financeRequest.getCalculationType();
        return switch (calculationType) {
            case EMI:
                yield calculateEmi(financeRequest);
            case TOTAL_AMOUNT:
                yield calculateFinalAmount(financeRequest);
            case PRINCIPAL_AMOUNT:
                yield null;
            case RATE_OF_INTEREST:
                yield new FinanceResponse().rateOfInterest(financeRequest.getRateOfInterest());
            case TIME_PERIOD:
//                return new FinanceResponse().timePeriod(financeRequest.getTimePeriod());
            case NONE:
               throw new IllegalArgumentException("Invalid calculation type");
        };
    }

    private FinanceResponse calculateEmi(FinanceRequest financeRequest) {

        double emiAmount = calculatedEmi(financeRequest);
        return new FinanceResponse().emiAmount((int) Math.ceil(emiAmount));
    }

    private FinanceResponse calculateFinalAmount(FinanceRequest financeRequest) {
        double finalAmount = calculatedEmi(financeRequest) * financeRequest.getTimePeriod();
        return new FinanceResponse().finalAmount(finalAmount);
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
