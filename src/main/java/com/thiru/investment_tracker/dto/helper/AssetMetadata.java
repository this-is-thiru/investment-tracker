package com.thiru.investment_tracker.dto.helper;

import com.thiru.investment_tracker.dto.enums.AccountType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(staticName = "from")
public class AssetMetadata {
	private AccountType accountType;
	private String accountHolder;
}
