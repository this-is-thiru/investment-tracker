package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.service.CorporateActionService;
import com.thiru.investment_tracker.service.TemporaryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{id}")
    public CorporateActionEntity getCorporateAction(@PathVariable String id) {
        return corporateActionService.getCorporateActionDetails(id);
    }

    @PutMapping("/update/priority/{id}/{priority}")
    public ResponseEntity<String> updateCorporateActionPriority(@PathVariable String id, @PathVariable int priority) {

        String message = corporateActionService.updateCorporateActionPriority(id, priority);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CorporateActionDto>> getCorporateActions(@RequestParam List<String> ids) {

        List<CorporateActionDto> actions = corporateActionService.getCorporateActions(ids);
        return ResponseEntity.ok(actions);
    }

    @PutMapping("/perform/{email}")
    public void performCorporateActions(@PathVariable String email) {
        corporateActionService.performPendingCorporateActions(email);
    }

    @GetMapping("/all")
    public List<CorporateActionEntity> deleteCorporateActions() {
        return corporateActionService.getAllCorporateActions();
    }

    @PreAuthorize("hasAnyRole('SUPER_USER')")
    @DeleteMapping("/delete/{id}")
    public void deleteCorporateActions(@PathVariable String id) {
        corporateActionService.deleteCorporateActions(id);
    }

    @PutMapping("/perform")
    public ResponseEntity<String> updateCorporateAction(@RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.performCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/perform/bonus/{email}")
    public ResponseEntity<String> updateCorporateActionBonus(@PathVariable String email, @RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.processBonusShares(email, corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/perform/test")
    public ResponseEntity<Boolean> updateCorporateActionBonus(@RequestBody CorporateActionDto request) {

        boolean message = temporaryTransactionService.anyCorporateActionToPerform(UserMail.from("test"), request.getStockCode(), request.getRecordDate());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/perform/test/{email}/{year}/{quarter}")
    public ResponseEntity<Boolean> updateCorporateActionBonus(@PathVariable String email, @PathVariable int year, @PathVariable int quarter) {

        corporateActionService.performQuarterlyCorporateActions(email, year, quarter);
        return ResponseEntity.ok(true);
    }
}
