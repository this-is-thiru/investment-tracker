package com.thiru.investment_tracker.core.controller;

import com.thiru.investment_tracker.core.dto.CorporateActionDto;
import com.thiru.investment_tracker.core.service.CorporateActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/corporate-action/user/{email}")
@RestController
public class UserCorporateActionController {

    private final CorporateActionService corporateActionService;

    @PutMapping("/perform/{month}/{year}")
    public void performCorporateActions(@PathVariable String email, @PathVariable String month, @PathVariable int year) {
        corporateActionService.performPendingCorporateActions(email, month, year);
    }

    @PutMapping("/perform/bonus")
    public ResponseEntity<String> updateCorporateActionBonus(@PathVariable String email, @RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.processBonusShares(email, corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/perform/test/{year}/{quarter}")
    public ResponseEntity<Boolean> updateCorporateActionBonus(@PathVariable String email, @PathVariable int year, @PathVariable int quarter) {

        corporateActionService.performQuarterlyCorporateActions(email, year, quarter);
        return ResponseEntity.ok(true);
    }
}
