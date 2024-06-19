package com.thiru.investment_tracker.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thiru.investment_tracker.common.ProfitAndLossContext;
import com.thiru.investment_tracker.common.ReportContext;
import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.common.enums.ParserDataType;
import com.thiru.investment_tracker.common.enums.TransactionType;
import com.thiru.investment_tracker.common.parser.ExcelParser;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.manager.TransactionParser;
import com.thiru.investment_tracker.operation.CriteriaBuilder;
import com.thiru.investment_tracker.operation.Filter;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.user.UserMail;
import com.thiru.investment_tracker.util.TransactionHeaders;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final TransactionService transactionService;
	private final ProfitAndLossService profitAndLossService;
	private final ReportService reportService;
	private final MongoTemplate mongoTemplate;

	private static final Set<String> NOT_ALLOWED_FIELDS = Set.of("email");

	@Transactional
	public String addTransaction(UserMail userMail, AssetRequest assetRequest) {

		if (assetRequest.getTransactionDate() == null) {
			assetRequest.setTransactionDate(LocalDate.now());
		}

		if (assetRequest.getTransactionType() == TransactionType.BUY) {
			buyStock(userMail, assetRequest);
			addTransactionInternal(assetRequest);
			return "Stock buy updated in portfolio";
		} else {
			sellStock(userMail, assetRequest);
			addTransactionInternal(assetRequest);
			return "Stock sell updated in portfolio and profit and loss updated in profit and loss";
		}
	}

	@Transactional
	public String uploadTransactions(UserMail userMail, MultipartFile file) {

		try {
			if (ExcelParser.isValidExcelFile(file)) {
				throw new BadRequestException("Invalid data format");
			}

			Map<String, ParserDataType> dataTypeMap = TransactionHeaders.getDataTypeMap();

			InputRecords inputRecords = ExcelParser.getRecordsFromExcel(file.getInputStream(), dataTypeMap);
			List<AssetRequest> assetRequests = TransactionParser.getTransactionRecords(inputRecords);
			TCommonUtil.map(assetRequests, assetRequest -> addTransaction(userMail, assetRequest));

			return "Transactions uploaded successfully";
		} catch (IOException e) {
			throw new BadRequestException("Invalid data format");
		}
	}

	public void buyStock(UserMail userMail, AssetRequest assetRequest) {

		String email = userMail.getEmail();
		assetRequest.setEmail(email);
		String stockCode = assetRequest.getStockCode();
		LocalDate transactionDate = assetRequest.getTransactionDate();

		Optional<Asset> optionalStock = portfolioRepository.findByEmailAndStockCodeAndTransactionDate(email, stockCode,
				transactionDate);
		Asset asset;
		if (optionalStock.isPresent()) {
			asset = optionalStock.get();
			long existingQuantity = asset.getQuantity();
			long newQuantity = existingQuantity + assetRequest.getQuantity();

			double existingTotalValue = asset.getTotalValue();
			double totalValueOfTransaction = getTotalValue(assetRequest);
			double newTotalValue = (existingTotalValue + totalValueOfTransaction);

			double newPrice = newTotalValue / newQuantity;

			asset.setPrice(newPrice);
			asset.setQuantity(newQuantity);
			asset.setTotalValue(newTotalValue);

			portfolioRepository.save(asset);
		} else {
			asset = TObjectMapper.copy(assetRequest, Asset.class);

			double totalValueOfTransaction = getTotalValue(assetRequest);
			asset.setTotalValue(totalValueOfTransaction);
			portfolioRepository.save(asset);
		}
	}

	public void sellStock(UserMail userMail, AssetRequest assetRequest) {

		String email = userMail.getEmail();
		String stockCode = assetRequest.getStockCode();

		List<Asset> stockEntities = portfolioRepository.findByEmailAndStockCodeOrderByTransactionDate(email, stockCode);

		validateTransaction(stockEntities, assetRequest);

		updateQuantity(userMail, stockEntities, assetRequest);
		portfolioRepository.saveAll(stockEntities);
	}

	private static void validateTransaction(List<Asset> stockEntities, AssetRequest assetRequest) {
		if (stockEntities.isEmpty()) {
			throw new IllegalArgumentException("Stock not found");
		}

		long existingQuantity = TCommonUtil.mapToLong(stockEntities, Asset::getQuantity).sum();

		if (existingQuantity < assetRequest.getQuantity()) {
			throw new IllegalArgumentException("Not enough stocks to sell");
		}
	}

	private void addTransactionInternal(AssetRequest assetRequest) {
		transactionService.addTransaction(assetRequest);
	}

	public List<AssetResponse> getStockQuantity(UserMail userMail, String stockCode) {

		String email = userMail.getEmail();

		List<Asset> stockEntities = portfolioRepository.findByEmailAndStockCodeOrderByTransactionDate(email, stockCode);

		if (stockEntities.isEmpty()) {
			throw new IllegalArgumentException("Stock not found");
		}

		return TCommonUtil.map(stockEntities, asset -> TObjectMapper.copy(asset, AssetResponse.class));
	}

	public List<AssetResponse> getAllStocks(UserMail userMail) {

		List<Asset> stockEntities = portfolioRepository.findByEmail(userMail.getEmail());

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(Asset::getStockCode));

		List<AssetResponse> responseEntities = TCommonUtil.map(stockEntityMap.values(),
				PortfolioService::combineAllDetailsOfEntities);

		log.info("Fetching portfolio stocks of {}", userMail.getEmail());
		return responseEntities;
	}

	public List<AssetResponse> getStocksWithDateRange(UserMail userMail, LocalDate startDate, LocalDate endDate) {

		String email = userMail.getEmail();

		List<Asset> stockEntities = portfolioRepository.findByEmailAndTransactionDateBetween(email, startDate, endDate);

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(Asset::getStockCode));
		List<AssetResponse> responseEntities = TCommonUtil.map(stockEntityMap.values(),
				PortfolioService::combineAllDetailsOfEntities);

		log.info("Fetching stocks from portfolio between {} and {}", startDate, endDate);
		return responseEntities;
	}

	/**
	 * A method that combines all details of stock entities to single entity based
	 * on a given stock code.
	 *
	 * @param stockEntities
	 *            a list of stock entities to search through* @return the response
	 *            entity that matches the provided stock code
	 */
	private static AssetResponse combineAllDetailsOfEntities(List<Asset> stockEntities) {
		AssetResponse assetResponse = TObjectMapper.copy(stockEntities.getFirst(), AssetResponse.class);

		double totalValue = 0;
		long quantity = 0;

		for (Asset entity : stockEntities) {

			totalValue += entity.getTotalValue();
			quantity += entity.getQuantity();
		}

		assetResponse.setQuantity(quantity);
		assetResponse.setTotalValue(totalValue);
		assetResponse.setPrice(totalValue / quantity);
		return assetResponse;
	}

	private static double getTotalValue(AssetRequest assetRequest) {
		return assetRequest.getPrice() * assetRequest.getQuantity();
	}

	private void updateQuantity(UserMail userMail, List<Asset> stockEntities, AssetRequest assetRequest) {

		long sellQuantity = assetRequest.getQuantity();

		Iterator<Asset> stockEntitiesIterator = stockEntities.iterator();
		while (sellQuantity > 0) {

			Asset asset = stockEntitiesIterator.next();
			Long assetQuantity = asset.getQuantity();

			ReportContext reportContext;
			ProfitAndLossContext profitAndLossContext;

			if (sellQuantity >= assetQuantity) {

				asset.setQuantity(0L);
				asset.setTotalValue(0);

				reportContext = toReportContext(asset, assetRequest, assetQuantity);
				profitAndLossContext = toProfitAndLossContext(asset, assetRequest, assetQuantity);
				sellQuantity = sellQuantity - assetQuantity;
			} else {

				long remainingQuantity = assetQuantity - sellQuantity;
				asset.setQuantity(remainingQuantity);
				asset.setTotalValue(remainingQuantity * asset.getPrice());

				reportContext = toReportContext(asset, assetRequest, sellQuantity);
				profitAndLossContext = toProfitAndLossContext(asset, assetRequest, sellQuantity);
				sellQuantity = 0;
			}

			reportService.stockReport(userMail, reportContext);
			profitAndLossService.updateProfitAndLoss(userMail, profitAndLossContext);
		}
	}

	private static ProfitAndLossContext toProfitAndLossContext(Asset asset, AssetRequest assetRequest,
			long sellQuantity) {
		double purchasePrice = asset.getPrice();
		LocalDate purchaseDate = asset.getTransactionDate();

		double sellPrice = assetRequest.getPrice();
		LocalDate sellDate = assetRequest.getTransactionDate();

		return ProfitAndLossContext.from(purchasePrice, purchaseDate, sellPrice, sellQuantity, sellDate);

	}

	private static ReportContext toReportContext(Asset asset, AssetRequest assetRequest, long sellQuantity) {

		ReportContext reportContext = ReportContext.empty();

		// Adding asset details to ReportContext
		reportContext.setStockCode(asset.getStockCode());
		reportContext.setStockName(asset.getStockName());
		reportContext.setExchangeName(asset.getExchangeName());
		reportContext.setBrokerName(asset.getBrokerName());
		reportContext.setTotalValue(asset.getTotalValue());
		reportContext.setAssetType(asset.getAssetType());
		reportContext.setPurchasePrice(asset.getPrice());
		reportContext.setPurchaseDate(asset.getTransactionDate());

		// Adding asset request details to ReportContext
		reportContext.setActor(assetRequest.getActor());
		reportContext.setSellPrice(assetRequest.getPrice());
		reportContext.setSellDate(assetRequest.getTransactionDate());

		// Adding sell quantity to ReportContext
		reportContext.setSellQuantity(sellQuantity);
		return reportContext;

	}

	public ProfitAndLossResponse getProfitAndLoss(UserMail userMail, String financialYear) {
		return profitAndLossService.getProfitAndLoss(userMail, financialYear);
	}

	public List<Asset> searchAssets(UserMail userMail, List<Filter> filters) {
		validateFilters(filters);

		addEmailToFilter(filters, userMail.getEmail());

		Query query = new Query();
		Set<Criteria> criterias = new HashSet<>();

		filters.forEach(filter -> CriteriaBuilder.constructCriteria(filter, criterias));
		criterias.forEach(query::addCriteria);

		return mongoTemplate.find(query, Asset.class);
	}

	private static void addEmailToFilter(List<Filter> filters, String email) {
		Filter filter = new Filter();
		filter.setFilterKey("email");
		filter.setValue(email);
		filter.setOperation(Filter.FilterOperation.EQUALS);
		filters.add(filter);
	}

	private static void validateFilters(List<Filter> filters) {

		List<Filter> invalidFilters = TCommonUtil.filter(filters, a -> NOT_ALLOWED_FIELDS.contains(a.getFilterKey()));

		List<String> invalidFieldsForFilter = TCommonUtil.map(invalidFilters, Filter::getFilterKey);

		if (!invalidFieldsForFilter.isEmpty()) {
			throw new BadRequestException("These fields are not allowed for filtering: " + invalidFieldsForFilter);
		}
	}
}
