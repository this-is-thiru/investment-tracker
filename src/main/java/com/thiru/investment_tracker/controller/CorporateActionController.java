package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.service.CorporateActionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/portfolio/user/{email}")
@RestController
public class CorporateActionController {
    private final CorporateActionService corporateActionService;

    @PutMapping("/stocks/perform/corporate-action")
    public ResponseEntity<String> updateCorporateAction(@PathVariable String email, @RequestBody CorporateActionWrapper corporateActionRequest) {

        String message = corporateActionService.updateCorporateAction(UserMail.from(email), corporateActionRequest);
        return ResponseEntity.ok(message);
    }
}
