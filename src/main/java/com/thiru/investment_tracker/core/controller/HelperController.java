package com.thiru.investment_tracker.core.controller;

import com.thiru.investment_tracker.core.dto.EntityExportRequest;
import com.thiru.investment_tracker.core.dto.user.UserMail;
import com.thiru.investment_tracker.core.file.FileHelper;
import com.thiru.investment_tracker.core.file.FileStream;
import com.thiru.investment_tracker.core.service.EntityExportService;
import com.thiru.investment_tracker.core.service.TransactionService;
import com.thiru.investment_tracker.core.util.transaction.ExcelHeaders;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("/helper")
@RestController
public class HelperController {

    private final EntityExportService entityExportService;
    private final TransactionService transactionService;

    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> getTemplate() {

        EntityExportRequest entityExportRequest = new EntityExportRequest();
        entityExportRequest.setEntityName("transactions-template");
        FileStream fileStream = entityExportService.export(UserMail.from(null), entityExportRequest);

        return FileHelper.sendFileAsAttachment(fileStream);
    }

    @GetMapping("/template/fields")
    public ResponseEntity<String[]> getTemplateFields() {

        String[] templateFields = ExcelHeaders.getTransactionHeaders();
        return ResponseEntity.ok(templateFields);
    }
}
