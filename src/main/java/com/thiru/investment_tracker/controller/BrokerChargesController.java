package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.AssetManagementDetailsRequest;
import com.thiru.investment_tracker.dto.BrokerChargesRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.BrokerCharges;
import com.thiru.investment_tracker.service.AssetManagementService;
import com.thiru.investment_tracker.service.BrokerChargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/broker-charges/")
@RestController
public class BrokerChargesController {

    private final BrokerChargeService brokerChargeService;
    private final AssetManagementService assetManagementService;

    @PostMapping("/add")
    public String addBrokerCharge(@RequestBody BrokerChargesRequest brokerChargesRequest) {
        return brokerChargeService.addBrokerCharge(brokerChargesRequest);
    }

    @GetMapping("/{id}")
    public BrokerCharges getBrokerCharge(@PathVariable String id) {
        return brokerChargeService.getBrokerCharges(id);
    }

    @PostMapping("/user/{email}/add/asset-management-detail")
    public void addAssetManagementDetailsEntry(@PathVariable String email, @RequestBody AssetManagementDetailsRequest request) {
        assetManagementService.addAssetManagementEntry(UserMail.from(email), request);
    }

    @PostMapping("/amc/impose")
    public void imposeAmcCharges() {
        assetManagementService.imposeAmcCharges();
    }
}
