package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.dto.CorporateActionPerformDto;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.service.CorporateActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/corporate-action/user/{email}")
@RestController
public class UserCorporateActionController {

    private final CorporateActionService corporateActionService;

    @PutMapping("/perform")
    public void performCorporateActions(@PathVariable String email, @RequestBody CorporateActionPerformDto actionPerformDto,
                                        @RequestParam(value = "allBrokers", required = false, defaultValue = "false") boolean allBrokers) {
        corporateActionService.performPendingCorporateActions(email, actionPerformDto, allBrokers);
    }
}
