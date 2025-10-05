package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.context.AmcChargesContext;
import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetManagementDetails;
import com.thiru.investment_tracker.repository.AssetManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetManagementService {
    private final AssetManagementRepository assetManagementRepository;
    private final ProfitAndLossService profitAndLossService;

    public void imposeAmcCharges() {
        LocalDate today = LocalDate.now();
        List<AssetManagementDetails> assetManagementDetails = assetManagementRepository.findByLastAmcChargesDeductedOnBefore(today);

        List<String> idsWithNullLastAmcChargesDate = new ArrayList<>();
        for (AssetManagementDetails assetManagementDetail : assetManagementDetails) {
            LocalDate fromDate = assetManagementDetail.getLastAmcChargesDeductedOn();
            if (fromDate == null) {
                idsWithNullLastAmcChargesDate.add(assetManagementDetail.getId());
                continue;
            }
            LocalDate toDate = fromDate.plusDays(90);
            assetManagementDetail.setLastAmcChargesDeductedOn(toDate);
            double annualAmcCharges = assetManagementDetail.getAmcCharges();
            double quarterlyCharges = annualAmcCharges / 4;
            var amcChargesEvent = imposeAmcCharges(assetManagementDetail, fromDate, quarterlyCharges);
            assetManagementDetail.getAmcChargesEvents().add(amcChargesEvent);
            assetManagementRepository.save(assetManagementDetail);
        }

        System.out.println("Update lastUpdated on and redrive the transactions for: " + idsWithNullLastAmcChargesDate);
    }

    public AssetManagementDetails.AmcChargesEvent imposeAmcCharges(AssetManagementDetails assetManagementDetail, LocalDate fromDate, double quarterlyCharges) {
        double taxes = assetManagementDetail.getAmcCharges() * assetManagementDetail.getTaxOnAmcCharges() / 100;
        LocalDate transactionDate = assetManagementDetail.getLastAmcChargesDeductedOn();
        AmcChargesContext amcChargesContext = new AmcChargesContext(assetManagementDetail.getBrokerName(), BrokerChargeTransactionType.AMC_CHARGES,
                transactionDate, quarterlyCharges, taxes);
        String id = profitAndLossService.updateProfitAndLossWithAmcCharges(UserMail.from(assetManagementDetail.getEmail()), amcChargesContext);
        return new AssetManagementDetails.AmcChargesEvent(id, transactionDate.plusDays(1), quarterlyCharges, List.of(fromDate, transactionDate));
    }
}
