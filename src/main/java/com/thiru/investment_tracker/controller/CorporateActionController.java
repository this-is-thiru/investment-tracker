package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.user.UserMail;
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
    public ResponseEntity<String> addCorporateAction(@RequestBody CorporateActionWrapper corporateActionRequest) {

        String message = corporateActionService.addCorporateAction(corporateActionRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("portfolio/user/{email}/perform")
    public ResponseEntity<String> updateCorporateAction(@PathVariable String email, @RequestBody CorporateActionWrapper corporateActionRequest) {

        String message = corporateActionService.updateCorporateAction(UserMail.from(email), corporateActionRequest);
        return ResponseEntity.ok(message);
    }
}
