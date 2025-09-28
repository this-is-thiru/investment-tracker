package com.thiru.investment_tracker.core.controller;

import com.thiru.investment_tracker.core.dto.EntityExportRequest;
import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.file.FileHelper;
import com.thiru.investment_tracker.core.file.FileStream;
import com.thiru.investment_tracker.core.service.EntityExportService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/portfolio/user/{email}")
@RestController
public class EntityExportController {

    private final EntityExportService entityExportService;

    @PostMapping("/stocks/download")
    public ResponseEntity<InputStreamResource> downloadPortfolio(@PathVariable String email, @RequestBody EntityExportRequest entityExportRequest) {

        FileStream fileStream = entityExportService.export(UserMail.from(email), entityExportRequest);
        return FileHelper.sendFileAsAttachment(fileStream);
    }

}
