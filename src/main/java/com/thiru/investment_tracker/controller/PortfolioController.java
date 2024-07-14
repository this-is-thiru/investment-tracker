package com.thiru.investment_tracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.BulkGetRequest;
import com.thiru.investment_tracker.dto.DeleteThisFile;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.TemporaryService;
import com.thiru.investment_tracker.service.TransactionService;
import com.thiru.investment_tracker.user.UserMail;

import lombok.AllArgsConstructor;

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

	@PostMapping("/request3/{message}")
	public ResponseEntity<String> testRequest(@PathVariable String email, @PathVariable String message) {

		temporaryService.producerMethod(message);
		return ResponseEntity.ok("Hey! Message seeded for process");
	}

}
