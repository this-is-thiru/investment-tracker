package com.thiru.investment_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InsuranceService {

    @Value("${insurance.service.url}")
    private String insuranceServiceUrl;

}
