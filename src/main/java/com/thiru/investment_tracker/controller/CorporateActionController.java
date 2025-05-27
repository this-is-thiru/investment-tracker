package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.service.CorporateActionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/corporate-action/")
@RestController
public class CorporateActionController {
    private final CorporateActionService corporateActionService;

    @PostMapping("/add")
    public ResponseEntity<String> addCorporateAction(@RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.addCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/perform")
    public ResponseEntity<String> updateCorporateAction(@RequestBody CorporateActionDto corporateActionRequest) {

        String message = corporateActionService.updateCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }
}
