package com.thiru.investment_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a shipping label with all necessary information for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingLabel {
    // Sender Information
    private String senderName;
    private String senderCompany;
    private String senderAddress1;
    private String senderAddress2;
    private String senderCity;
    private String senderState;
    private String senderZip;
    private String senderCountry;

    // Recipient Information
    private String recipientName;
    private String recipientCompany;
    private String recipientAddress1;
    private String recipientAddress2;
    private String recipientCity;
    private String recipientState;
    private String recipientZip;
    private String recipientCountry;

    // Shipping Information
    private String trackingNumber;
    private String serviceType;
    private String weight;
    private String packageId;
    private String dimensions;
    private String contents;
    private String notes;
}
