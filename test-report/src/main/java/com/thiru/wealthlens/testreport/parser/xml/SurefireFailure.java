package com.thiru.wealthlens.testreport.parser.xml;

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurefireFailure {
    @JacksonXmlProperty(isAttribute = true)
    private String message;

    @JacksonXmlProperty(isAttribute = true)
    private String type;

    @JacksonXmlText
    private String detail;
}