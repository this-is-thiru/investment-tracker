package com.thiru.investment_tracker.service.parser.model;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RequestParser<RequestType> {

    List<RequestType> parse(MultipartFile file, List<String> errors);

}
