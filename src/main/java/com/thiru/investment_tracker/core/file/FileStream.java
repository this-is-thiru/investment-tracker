package com.thiru.investment_tracker.core.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;

@Getter
@AllArgsConstructor(staticName = "from")
public class FileStream {
    private String fileName;
    private InputStreamResource inputStreamResource;
    private FileType fileType;
}
