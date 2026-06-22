package com.thiru.wealthlens.taxplanning.recommendation;

import com.thiru.wealthlens.taxplanning.enums.RegimeType;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RegimeAdvice {

    RegimeType recommendedRegime;
    long annualSaving;
    long newRegimeTotalTax;
    long oldRegimeTotalTax;
    List<String> switchTriggers;
    String taxYear;
}