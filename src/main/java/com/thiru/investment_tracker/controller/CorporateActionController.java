package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.service.CorporateActionService;
import com.thiru.investment_tracker.service.TemporaryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/corporate-action/")
@RestController
public class CorporateActionController {

    private final CorporateActionService corporateActionService;
    private final TemporaryTransactionService temporaryTransactionService;

    @PostMapping("/add")
    public ResponseEntity<String> addCorporateAction(@RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.addCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/list")
    public ResponseEntity< List<CorporateActionDto>> getCorporateActions(@RequestParam List<String> ids) {

        List<CorporateActionDto> actions = corporateActionService.getCorporateActions(ids);
        return ResponseEntity.ok(actions);
    }

    @PutMapping("/perform")
    public ResponseEntity<String> updateCorporateAction(@RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.updateCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/perform/bonus/{email}")
    public ResponseEntity<String> updateCorporateActionBonus(@PathVariable String email, @RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.processBonusShares(email, corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/perform/test")
    public ResponseEntity<Boolean> updateCorporateActionBonus(@RequestBody CorporateActionDto request) {

        boolean message = temporaryTransactionService.isCorporateActionToPerform(request.getStockCode(), request.getRecordDate());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/perform/test/{email}/{year}/{quarter}")
    public ResponseEntity<Boolean> updateCorporateActionBonus(@PathVariable String email, @PathVariable int year, @PathVariable int quarter) {

        corporateActionService.performQuarterlyCorporateActions(email, year, quarter);
        return ResponseEntity.ok(true);
    }
}
