package com.thiru.investment_tracker.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor(staticName = "from")
@EqualsAndHashCode(callSuper = true)
@Data
public class ProfitAndLossContext extends AssetContext {
}
