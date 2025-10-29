package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.context.BrokerChargeContext;
import com.thiru.investment_tracker.dto.enums.BrokerChargeTransactionType;
import com.thiru.investment_tracker.dto.request.AssetManagementDetailsRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetManagementDetails;
import com.thiru.investment_tracker.repository.AssetManagementRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
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
            LocalDate toDate = fromDate.plusDays(91);
            assetManagementDetail.setLastAmcChargesDeductedOn(toDate);
            var amcChargesEvent = imposeAmcCharges(assetManagementDetail, fromDate);
            assetManagementDetail.getAmcChargesEvents().add(amcChargesEvent);
            assetManagementRepository.save(assetManagementDetail);
        }

        log.info("Update lastUpdated on and redrive the transactions for: {}", idsWithNullLastAmcChargesDate);
    }

    private AssetManagementDetails.AmcChargesEvent imposeAmcCharges(AssetManagementDetails assetManagementDetail, LocalDate fromDate) {

        LocalDate transactionDate = assetManagementDetail.getLastAmcChargesDeductedOn();
        BrokerChargeContext brokerChargeContext = new BrokerChargeContext(null, null, assetManagementDetail.getBrokerName(),
                BrokerChargeTransactionType.AMC_CHARGES, transactionDate, null, null, 0);

        var userBrokerCharges = profitAndLossService.updateProfitAndLossWithAmcCharges(UserMail.from(assetManagementDetail.getEmail()), brokerChargeContext);
        double totalAmount = userBrokerCharges.getAmcCharges() + userBrokerCharges.getTaxes();
        return new AssetManagementDetails.AmcChargesEvent(userBrokerCharges.getId(), transactionDate.plusDays(1), totalAmount, List.of(fromDate, transactionDate));
    }

    public void addAssetManagementEntry(UserMail userMail, AssetManagementDetailsRequest request) {
        if (request.getLastAmcChargesDeductedOn() == null) {
            throw new IllegalArgumentException("Last Amc Charges Deducted On cannot be null");
        }
        Optional<AssetManagementDetails> assetManagementDetailsOpt = assetManagementRepository.findByEmailAndBrokerName(userMail.getEmail(), request.getBrokerName());
        if (assetManagementDetailsOpt.isPresent()) {
            String existingId = assetManagementDetailsOpt.get().getId();
            AssetManagementDetails assetManagementDetails = TObjectMapper.safeCopy(request, AssetManagementDetails.class);
            assetManagementDetails.setEmail(userMail.getEmail());
            assetManagementDetails.setId(existingId);
            assetManagementRepository.save(assetManagementDetails);
            return;
        }
        AssetManagementDetails assetManagementDetails = TObjectMapper.safeCopy(request, AssetManagementDetails.class);
        assetManagementDetails.setEmail(userMail.getEmail());
        assetManagementRepository.save(assetManagementDetails);
    }
}
