package com.thiru.wealthlens.dto;

import com.thiru.wealthlens.entity.query.QueryFilter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntityExportRequest {
    private String entityName;
    private List<String> emailAddresses = new ArrayList<>();
    private List<String> selectedColumns = new ArrayList<>();
    private List<QueryFilter> queryFilters = new ArrayList<>();
}
