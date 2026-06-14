package com.thiru.investment_tracker.testreport.parser;

import com.thiru.investment_tracker.testreport.parser.xml.SurefireTestSuite;

import java.nio.file.Path;
import java.util.List;

public interface SurefireReportParser {
    SurefireTestSuite parse(Path xmlFile);
    List<SurefireTestSuite> parseDirectory(Path directory);
}