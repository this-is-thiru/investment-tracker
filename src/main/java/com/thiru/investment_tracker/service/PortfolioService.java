package com.thiru.investment_tracker.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.TLocaleDate;
import com.thiru.investment_tracker.common.TObjectMapper;
import com.thiru.investment_tracker.common.parser.ExcelParser;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.ProfitAndLossContext;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.dto.ReportContext;
import com.thiru.investment_tracker.dto.enums.HoldingType;
import com.thiru.investment_tracker.dto.enums.ParserDataType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.operation.CriteriaBuilder;
import com.thiru.investment_tracker.operation.Filter;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.user.UserMail;
import com.thiru.investment_tracker.util.TransactionHeaders;
import com.thiru.investment_tracker.util.TransactionParser;

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

	@Transactional
	public String addTransaction(UserMail userMail, AssetRequest assetRequest) {

		sanitizeAssetRequest(assetRequest);

		TransactionType transactionType = assetRequest.getTransactionType();
		String message = switch (transactionType) {
			case BUY -> {
				buyStock(userMail, assetRequest);
				yield "Stock buy added to portfolio";
			}
			case SELL -> {
				sellStock(userMail, assetRequest);
				yield "Stock sell updated in portfolio and profit and loss updated in profit and loss";
			}
		};
		addTransactionInternal(assetRequest);

		return message;
	}

	private static void sanitizeAssetRequest(AssetRequest assetRequest) {
		if (assetRequest.getTransactionDate() == null) {
			assetRequest.setTransactionDate(LocalDate.now());
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
		String brokerName = assetRequest.getStockCode();
		String accountHolder = assetRequest.getAccountHolder();
		LocalDate transactionDate = assetRequest.getTransactionDate();

		Optional<Asset> optionalStock = portfolioRepository
				.findByEmailAndStockCodeAndBrokerNameAndAccountHolderAndTransactionDate(email, stockCode, accountHolder,
						brokerName, transactionDate);
		Asset asset;
		if (optionalStock.isPresent()) {
			asset = optionalStock.get();
			double existingQuantity = asset.getQuantity();
			double newQuantity = existingQuantity + assetRequest.getQuantity();

			double existingTotalValue = asset.getTotalValue();
			double totalValueOfTransaction = getTotalValue(assetRequest);
			double newTotalValue = (existingTotalValue + totalValueOfTransaction);
			double newPrice = newTotalValue / newQuantity;
			double existingBrokerCharge = asset.getBrokerCharges();
			double existingMiscCharges = asset.getMiscCharges();

			asset.setBrokerCharges(assetRequest.getBrokerCharges() + existingBrokerCharge);
			asset.setMiscCharges(assetRequest.getMiscCharges() + existingMiscCharges);
			asset.setPrice(newPrice);
			asset.setQuantity(newQuantity);
			asset.setTotalValue(newTotalValue);
		} else {
			asset = TObjectMapper.copy(assetRequest, Asset.class);
			double totalValueOfTransaction = getTotalValue(assetRequest);

			asset.setTotalValue(totalValueOfTransaction);
		}

		portfolioRepository.save(asset);
	}

	public void sellStock(UserMail userMail, AssetRequest assetRequest) {

		String email = userMail.getEmail();
		String stockCode = assetRequest.getStockCode();
		String brokerName = assetRequest.getBrokerName();
		String accountHolder = assetRequest.getAccountHolder();

		List<Asset> stockEntities = portfolioRepository
				.findByEmailAndStockCodeAndBrokerNameAndAccountHolderOrderByTransactionDate(email, stockCode,
						brokerName, accountHolder);

		validateTransaction(stockEntities, assetRequest);

		updateQuantity(userMail, stockEntities, assetRequest);
		List<String> updatedStockEntities = TCommonUtil.applyMap(stockEntities, asset -> asset.getQuantity() == 0,
				Asset::getId);
		portfolioRepository.saveAll(stockEntities);

		if (!updatedStockEntities.isEmpty()) {
			portfolioRepository.deleteAllById(updatedStockEntities);
		}
	}

	private static void validateTransaction(List<Asset> stockEntities, AssetRequest assetRequest) {
		if (stockEntities.isEmpty()) {
			throw new IllegalArgumentException("Stock not found");
		}

		double existingQuantity = TCommonUtil.mapToDouble(stockEntities, Asset::getQuantity).sum();

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
				.collect(Collectors.groupingBy(stockWithCodeAndBroker()));

		List<AssetResponse> responseEntities = TCommonUtil.map(stockEntityMap.values(),
				PortfolioService::combineAllDetailsOfEntities);

		log.info("Fetching portfolio stocks of {}", userMail.getEmail());
		return responseEntities;
	}

	public List<AssetResponse> getStocksWithDateRange(UserMail userMail, LocalDate startDate, LocalDate endDate) {

		String email = userMail.getEmail();

		List<Asset> stockEntities = portfolioRepository.findByEmailAndTransactionDateBetween(email, startDate, endDate);

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(stockWithCodeAndBroker()));
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
	 *            a list of stock entities to search through
	 * @return the response entity that matches the provided stock code
	 */
	private static AssetResponse combineAllDetailsOfEntities(List<Asset> stockEntities) {
		AssetResponse assetResponse = TObjectMapper.copy(stockEntities.getFirst(), AssetResponse.class);

		double totalValue = 0;
		double quantity = 0;
		double brokerCharges = 0;
		double miscCharges = 0;
		List<String> transactionDates = new ArrayList<>();

		for (Asset entity : stockEntities) {
			totalValue += entity.getTotalValue();
			quantity += entity.getQuantity();
			brokerCharges += entity.getBrokerCharges();
			miscCharges += entity.getMiscCharges();
			transactionDates.add(TLocaleDate.convertToString(entity.getTransactionDate()));
		}

		assetResponse.setQuantity(quantity);
		assetResponse.setTotalValue(totalValue);
		assetResponse.setPrice(totalValue / quantity);
		assetResponse.setBrokerCharges(brokerCharges);
		assetResponse.setMiscCharges(miscCharges);
		assetResponse.setTransactionDates(transactionDates);
		assetResponse.setTransactionDate(null);
		return assetResponse;
	}

	private static double getTotalValue(AssetRequest assetRequest) {
		return assetRequest.getPrice() * assetRequest.getQuantity();
	}

	private void updateQuantity(UserMail userMail, List<Asset> stockEntities, AssetRequest assetRequest) {

		double sellQuantity = assetRequest.getQuantity();

		Iterator<Asset> stockEntitiesIterator = stockEntities.iterator();
		while (sellQuantity > 0) {

			Asset asset = stockEntitiesIterator.next();
			Double assetQuantity = asset.getQuantity();

			ReportContext reportContext;
			ProfitAndLossContext profitAndLossContext;

			if (sellQuantity >= assetQuantity) {
				reportContext = toReportContext(asset, assetRequest, assetQuantity);
				profitAndLossContext = ProfitAndLossContext.from(asset, assetRequest, assetQuantity);

				asset.setQuantity(0D);
				asset.setTotalValue(0);
				sellQuantity = sellQuantity - assetQuantity;
			} else {
				reportContext = toReportContext(asset, assetRequest, sellQuantity);
				profitAndLossContext = ProfitAndLossContext.from(asset, assetRequest, sellQuantity);

				double remainingQuantity = assetQuantity - sellQuantity;
				asset.setQuantity(remainingQuantity);
				asset.setTotalValue(remainingQuantity * asset.getPrice());
				sellQuantity = 0;
			}

			reportService.stockReport(userMail, reportContext);
			profitAndLossService.updateProfitAndLoss(userMail, profitAndLossContext);
		}
	}

	private static ReportContext toReportContext(Asset asset, AssetRequest assetRequest, double sellQuantity) {

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

		// Add AccountType and AccountHolder
		reportContext.setAccountType(assetRequest.getAccountType());
		reportContext.setAccountHolder(assetRequest.getAccountHolder());

		// Adding sell quantity to ReportContext
		reportContext.setSellQuantity(sellQuantity);
		return reportContext;

	}

	public ProfitAndLossResponse getProfitAndLoss(UserMail userMail, String financialYear) {
		return profitAndLossService.getProfitAndLoss(userMail, financialYear);
	}

	public List<Asset> searchAssets(UserMail userMail, List<Filter> filters) {

		isFiltersHavingEmail(filters);
		addEmailToFilter(filters, userMail.getEmail());
		validateFilters(filters);

		Query query = new Query();
		Set<Criteria> criteriaSet = new HashSet<>();

		filters.forEach(filter -> CriteriaBuilder.constructCriteria(filter, criteriaSet));
		criteriaSet.forEach(query::addCriteria);

		return mongoTemplate.find(query, Asset.class);
	}

	public String clearAllRecordsForCustomer(UserMail userMail) {

		log.info("Initiated deletion of all records of user: {}", userMail.getEmail());

		portfolioRepository.deleteByEmail(userMail.getEmail());
		log.info("Deleted all portfolio stocks for user: {}", userMail.getEmail());
		reportService.deleteReports(userMail);
		log.info("Deleted all reports for user: {}", userMail.getEmail());
		transactionService.deleteTransactions(userMail);
		log.info("Deleted all transactions for user: {}", userMail.getEmail());
		profitAndLossService.deleteProfitAndLoss(userMail);
		log.info("Deleted all profit and loss reports for user: {}", userMail.getEmail());

		return "User: " + userMail.getEmail() + ", records and transactions deleted successfully";
	}

	public List<AssetResponse> getAssets(UserMail userMail, HoldingType holdingType) {

		String oneYearBeforeDate = TLocaleDate.lastYearSameDateInString();

		List<Asset> assets = switch (holdingType) {
			case LONG_TERM -> getLongTermHeldAssets(userMail, oneYearBeforeDate);
			case SHORT_TERM -> getShortTermHeldAssets(userMail, oneYearBeforeDate);
		};

		Map<String, List<Asset>> resultedAssetsMap = assets.stream()
				.collect(Collectors.groupingBy(stockWithCodeAndBroker()));
		List<AssetResponse> resultedAssets = TCommonUtil.map(resultedAssetsMap.values(),
				PortfolioService::combineAllDetailsOfEntities);

		List<String> stockCodes = TCommonUtil.map(resultedAssets, AssetResponse::getStockCode);
		Map<String, Double> assetQuantityMap = TCommonUtil.toMap(resultedAssets, stockCodeWithBroker(),
				AssetResponse::getQuantity);

		List<AssetResponse> allAssets = getStockEntities(userMail, new HashSet<>(stockCodes));

		Function<AssetResponse, AssetResponse> quantityUpdater = assetResponse -> {
			double quantity = assetResponse.getQuantity();
			assetResponse.setTotalQuantity(quantity);
			String stockWithBroker = assetResponse.getStockCode() + assetResponse.getBrokerName();
			assetResponse.setQuantity(assetQuantityMap.get(stockWithBroker));
			return assetResponse;
		};

		return TCommonUtil.map(allAssets, quantityUpdater);
	}

	private Function<Asset, String> stockWithCodeAndBroker() {
		return asset -> asset.getStockCode() + asset.getBrokerName();
	}

	private Function<AssetResponse, String> stockCodeWithBroker() {
		return assetResponse -> assetResponse.getStockCode() + assetResponse.getBrokerName();
	}

	private List<AssetResponse> getStockEntities(UserMail userMail, Collection<String> stockCodes) {

		List<Asset> stockEntities = portfolioRepository.findByEmailAndStockCodeIn(userMail.getEmail(), stockCodes);

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(stockWithCodeAndBroker()));

		return TCommonUtil.map(stockEntityMap.values(), PortfolioService::combineAllDetailsOfEntities);
	}

	private List<Asset> getLongTermHeldAssets(UserMail userMail, String oneYearBeforeDate) {
		Filter filter = new Filter();
		filter.setFilterKey("transaction_date");
		filter.setValue(oneYearBeforeDate);
		filter.setOperation(Filter.FilterOperation.LESSER_THAN);
		filter.setIsDateField(true);

		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		return searchAssets(userMail, filters);
	}

	private List<Asset> getShortTermHeldAssets(UserMail userMail, String oneYearBeforeDate) {
		Filter filter = new Filter();
		filter.setFilterKey("transaction_date");
		filter.setValue(oneYearBeforeDate);
		filter.setOperation(Filter.FilterOperation.GREATER_THAN);
		filter.setIsDateField(true);

		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		return searchAssets(userMail, filters);
	}

	private static void addEmailToFilter(List<Filter> filters, String email) {
		Filter filter = new Filter();
		filter.setFilterKey("email");
		filter.setValue(email);
		filter.setOperation(Filter.FilterOperation.EQUALS);
		filters.add(filter);
	}

	private static void isFiltersHavingEmail(List<Filter> filters) {

		List<Filter> invalidFilters = TCommonUtil.filter(filters, filter -> Asset.EMAIL.equals(filter.getFilterKey()));

		if (!invalidFilters.isEmpty()) {
			throw new BadRequestException("Kindly remove email filter from payload");
		}
	}

	private static void validateFilters(List<Filter> filters) {

		List<Filter> invalidFilters = TCommonUtil.filter(filters,
				filter -> !Asset.ALLOWED_FIELDS.contains(filter.getFilterKey()));

		List<String> invalidFieldsForFilter = TCommonUtil.map(invalidFilters, Filter::getFilterKey);

		if (!invalidFieldsForFilter.isEmpty()) {
			throw new BadRequestException("These fields are not allowed for filtering: " + invalidFieldsForFilter);
		}
	}

	public ByteArrayInputStream downloadTemplate() {
		return TransactionHeaders.downloadTemplate();
	}
}
