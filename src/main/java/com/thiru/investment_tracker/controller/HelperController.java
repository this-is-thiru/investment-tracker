package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.EntityExportRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.helper.file.FileHelper;
import com.thiru.investment_tracker.helper.file.FileStream;
import com.thiru.investment_tracker.service.EntityExportService;
import com.thiru.investment_tracker.util.transaction.ExcelHeaders;
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

    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> getTemplate() {

        EntityExportRequest entityExportRequest = new EntityExportRequest();
        entityExportRequest.setEntityName("transactions-template");
        FileStream fileStream = entityExportService.export(UserMail.from(null), entityExportRequest);

        return FileHelper.sendFileAsAttachment(fileStream, "template.xlsx");
    }

    @GetMapping("/template/fields")
    public ResponseEntity<String[]> getTemplateFields() {

        String[] templateFields = ExcelHeaders.getTransactionHeaders();
        return ResponseEntity.ok(templateFields);
    }
}
