package com.thiru.investment_tracker.helper.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public enum FileType {
    XLSX(".xlsx", null, "application/vnd.ms-excel"),
    XLS(".xls", null, "application/vnd.ms-excel");

    private final String extension;
    private final String contentType;
    private final String mediaType;

    @JsonIgnore
    public MediaType getMediaType() {
        return MediaType.parseMediaType(mediaType);
    }

    FileType(String extension, String contentType, String mediaType) {
        this.extension = extension;
        this.contentType = contentType;
        this.mediaType = mediaType;
    }
}
