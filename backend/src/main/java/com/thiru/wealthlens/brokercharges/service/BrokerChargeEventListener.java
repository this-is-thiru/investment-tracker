package com.thiru.wealthlens.brokercharges.service;

import com.thiru.wealthlens.portfolio.api.event.TransactionSavedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BrokerChargeEventListener {

    @EventListener
    public void handle(TransactionSavedEvent event) {
        // Process transaction saved event for broker charges
    }
}
