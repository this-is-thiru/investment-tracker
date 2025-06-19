package com.thiru.investment_tracker.helper.file;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class FileHelper {
    public static ResponseEntity<InputStreamResource> sendFileAsAttachment(FileStream fileStream) {

        String headerValue = "attachment; filename=" + fileStream.getFileName();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(fileStream.getFileType().getMediaType()).body(fileStream.getInputStreamResource());
    }

    public static ResponseEntity<InputStreamResource> sendFileAsAttachment(FileStream fileStream, String fileName) {

        String headerValue = "attachment; filename=" + fileName;
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(fileStream.getFileType().getMediaType()).body(fileStream.getInputStreamResource());
    }
}
