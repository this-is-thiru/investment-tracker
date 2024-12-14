package com.thiru.investment_tracker.controller;

import com.thiru.investment_tracker.dto.*;
import com.thiru.investment_tracker.dto.enums.HoldingType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.TemporaryService;
import com.thiru.investment_tracker.service.TransactionService;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RequestMapping("/portfolio/user/{email}")
@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final TemporaryService temporaryService;

    @PostMapping("/transaction")
    public ResponseEntity<String> addTransaction(@PathVariable String email, @RequestBody AssetRequest assetRequest) {
        return ResponseEntity.ok(portfolioService.addTransaction(UserMail.from(email), assetRequest));
    }

    @PostMapping("/upload-transactions")
    public ResponseEntity<String> uploadTransactions(@PathVariable String email,
                                                     @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(portfolioService.uploadTransactions(UserMail.from(email), file));
    }

    // @PostMapping("/insurance")
    // public ResponseEntity<String> addInsurance(@PathVariable String email,
    // @RequestBody AssetRequest assetRequest) {
    // return
    // ResponseEntity.ok(portfolioService.addTransaction(UserMail.from(email),
    // assetRequest));
    // }

    @GetMapping("/profit-and-loss")
    public ResponseEntity<ProfitAndLossResponse> getProfitAndLoss(@PathVariable String email,
                                                                  @RequestParam String financialYear) {
        return ResponseEntity.ok(portfolioService.getProfitAndLoss(UserMail.from(email), financialYear));
    }

    @PostMapping("/clear/all")
    public ResponseEntity<String> getProfitAndLoss(@PathVariable String email) {
        return ResponseEntity.ok(portfolioService.clearAllRecordsForCustomer(UserMail.from(email)));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AssetResponse>> getAllStocks(@PathVariable String email) {
        return ResponseEntity.ok(portfolioService.getAllStocks(UserMail.from(email)));
    }

    @GetMapping("/mfs")
    public ResponseEntity<List<AssetResponse>> getAllMutualFunds(@PathVariable String email) {
        return ResponseEntity.ok(portfolioService.getMutualFunds(UserMail.from(email)));
    }

    @PostMapping("/allStocks")
    public ResponseEntity<List<AssetResponse>> getAllStocks(@PathVariable String email,
                                                            @RequestBody BulkGetRequest bulkGetRequest) {

        LocalDate startDate = bulkGetRequest.getDateRange().getStartDate();
        LocalDate endDate = bulkGetRequest.getDateRange().getEndDate();
        return ResponseEntity.ok(portfolioService.getStocksWithDateRange(UserMail.from(email), startDate, endDate));
    }

    @GetMapping("/stock/{stockCode}/all")
    public ResponseEntity<List<AssetResponse>> getAllStocks(@PathVariable String email,
                                                            @PathVariable String stockCode) {

        return ResponseEntity.ok(portfolioService.getStockQuantity(UserMail.from(email), stockCode));
    }

    @GetMapping("/all/transactions")
    public ResponseEntity<List<Transaction>> allTransactions(@PathVariable String email) {

        UserMail.from(email);
        return ResponseEntity.ok(transactionService.allTransactions());
    }

    @GetMapping("/all/assets")
    public ResponseEntity<List<AssetResponse>> allAssets(@PathVariable String email, @RequestParam("type") String type) {
        List<AssetResponse> assets = portfolioService.getAssets(UserMail.from(email), HoldingType.valueOf(type));
        return ResponseEntity.ok(assets);
    }

    @PutMapping("/stocks/download")
    public ResponseEntity<InputStreamResource> downloadPortfolioStocks(@PathVariable String email) {

        Pair<InputStreamResource, String> resourcePair = portfolioService.downloadPortfolioStocks(UserMail.from(email));

        String mediaType = "application/vnd.ms-excel";
        String headerValue = "attachment; filename=" + resourcePair.getSecond();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.parseMediaType(mediaType)).body(resourcePair.getFirst());
    }

    // Note: Below this comment is for testing purpose only

    @PostMapping("/request")
    public ResponseEntity<AssetRequest> testRequest(@RequestBody AssetRequest assetRequest) {

        if (assetRequest.getTransactionDate() == null) {
            assetRequest.setTransactionDate(LocalDate.now());
        }
        return ResponseEntity.ok(assetRequest);
    }

    @PostMapping("/request1")
    public ResponseEntity<DeleteThisFile> testRequest(@RequestBody ProfitAndLossResponse requestEntity) {

        DeleteThisFile deleteThisFile = TObjectMapper.copy(requestEntity, DeleteThisFile.class);
        return ResponseEntity.ok(deleteThisFile);
    }

    @PostMapping("/request2")
    public ResponseEntity<List<Asset>> testRequest(@PathVariable String email,
                                                   @RequestBody BulkGetRequest bulkGetRequest) {
        List<Asset> assets = portfolioService.searchAssets(UserMail.from(email), bulkGetRequest.getFilters());
        return ResponseEntity.ok(assets);
    }

}
