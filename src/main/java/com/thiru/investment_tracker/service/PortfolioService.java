package com.thiru.investment_tracker.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.Enums.TransactionType;
import com.thiru.investment_tracker.common.ProfitAndLossContext;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.ProfitAndLossResponse;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.operation.CriteriaBuilder;
import com.thiru.investment_tracker.operation.Filter;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.user.UserMail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final TransactionService transactionService;
	private final ProfitAndLossService profitAndLossService;
	private final MongoTemplate mongoTemplate;

	public String addTransaction(UserMail userMail, AssetRequest assetRequest) {

		if (assetRequest.getTransactionDate() == null) {
			assetRequest.setTransactionDate(new Date());
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

	public void buyStock(UserMail userMail, AssetRequest assetRequest) {

		String email = userMail.getEmail();
		String stockCode = assetRequest.getStockCode();
		Date transactionDate = assetRequest.getTransactionDate();

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
			asset = CommonUtil.copy(assetRequest, Asset.class);

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

		long existingQuantity = CommonUtil.mapToLong(stockEntities, Asset::getQuantity).sum();

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

		return CommonUtil.map(stockEntities, asset -> CommonUtil.copy(asset, AssetResponse.class));
	}

	public List<AssetResponse> getAllStocks(UserMail userMail) {

		List<Asset> stockEntities = portfolioRepository.findByEmail(userMail.getEmail());

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(Asset::getStockCode));

		List<AssetResponse> responseEntities = CommonUtil.map(stockEntityMap.values(),
				PortfolioService::combineAllDetailsOfEntities);

		log.info("Fetching portfolio stocks of {}", userMail.getEmail());
		return responseEntities;
	}

	public List<AssetResponse> getStocksWithDateRange(UserMail userMail, Date startDate, Date endDate) {

		String email = userMail.getEmail();

		List<Asset> stockEntities = portfolioRepository.findByEmailAndTransactionDateBetween(email, startDate, endDate);

		Map<String, List<Asset>> stockEntityMap = stockEntities.stream()
				.collect(Collectors.groupingBy(Asset::getStockCode));
		List<AssetResponse> responseEntities = CommonUtil.map(stockEntityMap.values(),
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
		AssetResponse assetResponse = CommonUtil.copy(stockEntities.getFirst(), AssetResponse.class);

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

		long quantity = assetRequest.getQuantity();

		Iterator<Asset> stockEntitiesIterator = stockEntities.iterator();
		while (quantity > 0) {

			Asset asset = stockEntitiesIterator.next();

			if (quantity >= asset.getQuantity()) {

				asset.setQuantity(0L);
				asset.setTotalValue(0);

				ProfitAndLossContext context = toProfitAndLossContext(asset, assetRequest);
				profitAndLossService.updateProfitAndLoss(userMail, context);
				quantity = quantity - asset.getQuantity();
			} else {

				long remainingQuantity = asset.getQuantity() - quantity;
				asset.setQuantity(remainingQuantity);
				asset.setTotalValue(remainingQuantity * asset.getPrice());
				ProfitAndLossContext context = toProfitAndLossContext(asset, assetRequest);
				profitAndLossService.updateProfitAndLoss(userMail, context);
				quantity = 0;
			}
		}
	}

	private static ProfitAndLossContext toProfitAndLossContext(Asset asset, AssetRequest assetRequest) {
		double purchasePrice = asset.getPrice();
		Date purchaseDate = asset.getTransactionDate();

		double sellPrice = assetRequest.getPrice();
		long sellQuantity = assetRequest.getQuantity();
		Date sellDate = assetRequest.getTransactionDate();

		return ProfitAndLossContext.from(purchasePrice, purchaseDate, sellPrice, sellQuantity, sellDate);

	}

	public ProfitAndLossResponse getProfitAndLoss(UserMail userMail, String financialYear) {
		return profitAndLossService.getProfitAndLoss(userMail, financialYear);
	}

	public List<Asset> searchAssets(UserMail userMail, List<Filter> filters) {
		addEmailToFilter(filters, userMail.getEmail());

		Query query = new Query();
		filters.forEach(filter -> query.addCriteria(CriteriaBuilder.constructCriteria(filter)));

		return mongoTemplate.find(query, Asset.class);
	}

	private static void addEmailToFilter(List<Filter> filters, String email) {
		Filter filter = new Filter();
		filter.setFilterKey("email");
		filter.setValue(email);
		filter.setOperation(Filter.FilterOperation.EQUALS);
		filters.add(filter);
	}
}
