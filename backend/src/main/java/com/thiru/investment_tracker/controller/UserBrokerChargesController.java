package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.UserBrokerCharges;
import com.thiru.investment_tracker.service.UserBrokerChargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/user-broker-charges/user/{email}")
@RestController
public class UserBrokerChargesController {

    private final UserBrokerChargeService userBrokerChargesService;

    @GetMapping("/all")
    public List<UserBrokerCharges> getUserBrokerCharges(@PathVariable String email) {
        return userBrokerChargesService.getUserBrokerCharges(UserMail.from(email));
    }
}
