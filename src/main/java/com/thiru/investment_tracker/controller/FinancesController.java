package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.model.FinanceRequest;
import com.thiru.investment_tracker.model.FinanceResponse;
import com.thiru.investment_tracker.model.InterestRateResponse;
import com.thiru.investment_tracker.model.StepUpSIPRequest;
import com.thiru.investment_tracker.service.FinancesService;

import com.thiru.investment_tracker.service.StepUpSIPCalculator;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finances")
@AllArgsConstructor
public class FinancesController {

    private final FinancesService financesService;
    private final StepUpSIPCalculator sipCalculator;

    @PostMapping("/calculate")
    public FinanceResponse calculate(@RequestBody FinanceRequest financeRequest) {
        return financesService.getFinanceResponse(financeRequest);
    }

    @PostMapping("/interest-rate")
    public InterestRateResponse calculateInterestRate(@RequestBody StepUpSIPRequest financeRequest) {
        return sipCalculator.calculateRate(financeRequest);
    }
}
