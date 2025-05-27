package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.*;
import com.thiru.investment_tracker.dto.enums.*;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.repository.PortfolioRepository;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TLocaleDate;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.util.db.QueryFilter;
import com.thiru.investment_tracker.util.parser.ExcelBuilder;
import com.thiru.investment_tracker.util.parser.ExcelParser;
import com.thiru.investment_tracker.util.transaction.ExcelHeaders;
import com.thiru.investment_tracker.util.transaction.TransactionParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionService transactionService;
    private final ProfitAndLossService profitAndLossService;
    private final ReportService reportService;
    private final MongoTemplateService mongoTemplateService;

    @Transactional
    public String addTransaction(UserMail userMail, AssetRequest assetRequest) {

        sanitizeAssetRequest(assetRequest);

        TransactionType transactionType = assetRequest.getTransactionType();
        // Add transaction
        String transactionId = addTransactionInternal(assetRequest);

        return switch (transactionType) {
            case BUY -> {
                buyStock(userMail, transactionId, assetRequest);
                yield "Stock buy added to portfolio";
            }
            case SELL -> {
                sellStock(userMail, transactionId, assetRequest);
                yield "Stock sell updated in portfolio and profit and loss updated in profit and loss";
            }
        };
    }

    private static void sanitizeAssetRequest(AssetRequest assetRequest) {
        if (assetRequest.getTransactionDate() == null) {
            assetRequest.setTransactionDate(LocalDate.now());
        }

        // Convert local date time to instant
        LocalDateTime orderExecutionTime = assetRequest.getOrderDateTime();
        if (orderExecutionTime != null) {
            ZoneId zoneId = ZoneId.of(assetRequest.getTimezoneId());
            ZonedDateTime zonedDateTime = orderExecutionTime.atZone(zoneId);
            assetRequest.setOrderExecutionTime(zonedDateTime.toInstant());
        }
    }

    @Transactional
    public String uploadTransactions(UserMail userMail, MultipartFile file) {

        try {
            if (ExcelParser.isValidExcelFile(file)) {
                throw new BadRequestException("Invalid data format");
            }

            Map<String, ParserDataType> dataTypeMap = ExcelHeaders.getDataTypeMap();

            InputRecords inputRecords = ExcelParser.getRecordsFromExcel(file.getInputStream(), dataTypeMap);
            List<AssetRequest> assetRequests = TransactionParser.getTransactionRecords(inputRecords);
            TCollectionUtil.map(assetRequests, assetRequest -> addTransaction(userMail, assetRequest));

            return "Transactions uploaded successfully";
        } catch (IOException e) {
            throw new BadRequestException("Invalid data format");
        }
    }

    public void buyStock(UserMail userMail, String transactionId, AssetRequest assetRequest) {

        String email = userMail.getEmail();
        assetRequest.setEmail(email);
        String stockCode = assetRequest.getStockCode();
        BrokerName brokerName = assetRequest.getBrokerName();
        String accountHolder = assetRequest.getAccountHolder();
        LocalDate transactionDate = assetRequest.getTransactionDate();

        Optional<AssetEntity> optionalStock = portfolioRepository
                .findByEmailAndStockCodeAndBrokerNameAndAccountHolderAndTransactionDate(email, stockCode,
                        brokerName, accountHolder, transactionDate);
        AssetEntity assetEntity;
        if (optionalStock.isPresent()) {
            assetEntity = optionalStock.get();
            double existingQuantity = assetEntity.getQuantity();
            double newQuantity = existingQuantity + assetRequest.getQuantity();

            double existingTotalValue = assetEntity.getTotalValue();
            double totalValueOfTransaction = getTotalValue(assetRequest);
            double newTotalValue = (existingTotalValue + totalValueOfTransaction);
            double newPrice = newTotalValue / newQuantity;
            double existingBrokerCharge = assetEntity.getBrokerCharges();
            double existingMiscCharges = assetEntity.getMiscCharges();

            assetEntity.setBrokerCharges(assetRequest.getBrokerCharges() + existingBrokerCharge);
            assetEntity.setMiscCharges(assetRequest.getMiscCharges() + existingMiscCharges);
            assetEntity.setPrice(newPrice);
            assetEntity.setQuantity(newQuantity);
            assetEntity.setTotalValue(newTotalValue);

            OrderTimeQuantity orderTimeQuantity = new OrderTimeQuantity();
            orderTimeQuantity.setOrderExecutionTime(assetRequest.getOrderExecutionTime());
            orderTimeQuantity.setQuantity(assetRequest.getQuantity());

            assetRequest.getOrderTimeQuantities().add(orderTimeQuantity);
        } else {
            assetEntity = assetRequest.getAsset();
            double totalValueOfTransaction = getTotalValue(assetRequest);

            assetEntity.setTotalValue(totalValueOfTransaction);

            OrderTimeQuantity orderTimeQuantity = new OrderTimeQuantity();
            orderTimeQuantity.setOrderExecutionTime(assetRequest.getOrderExecutionTime());
            orderTimeQuantity.setQuantity(assetRequest.getQuantity());

            assetEntity.getOrderTimeQuantities().add(orderTimeQuantity);
        }

        assetEntity.getBuyTransactionIds().add(transactionId);
        portfolioRepository.save(assetEntity);
    }

    public void sellStock(UserMail userMail, String transactionId, AssetRequest assetRequest) {

        String email = userMail.getEmail();
        String stockCode = assetRequest.getStockCode();
        BrokerName brokerName = assetRequest.getBrokerName();
        String accountHolder = assetRequest.getAccountHolder();

        List<AssetEntity> stockEntities = portfolioRepository
                .findByEmailAndStockCodeAndBrokerNameAndAccountHolderOrderByTransactionDate(email, stockCode,
                        brokerName, accountHolder);

        validateTransaction(stockEntities, assetRequest);

        updateQuantity(userMail, transactionId, stockEntities, assetRequest);
        List<String> updatedStockEntities = TCollectionUtil.applyMap(stockEntities, asset -> asset.getQuantity() == 0,
                AssetEntity::getId);
        portfolioRepository.saveAll(stockEntities);

        if (!updatedStockEntities.isEmpty()) {
            portfolioRepository.deleteAllById(updatedStockEntities);
        }
    }

    private static void validateTransaction(List<AssetEntity> stockEntities, AssetRequest assetRequest) {
        if (stockEntities.isEmpty()) {
            throw new IllegalArgumentException("Stock not found");
        }

        double existingQuantity = TCollectionUtil.mapToDouble(stockEntities, AssetEntity::getQuantity).sum();

        if (existingQuantity < assetRequest.getQuantity()) {
            throw new IllegalArgumentException("Not enough stocks to sell");
        }
    }

    private String addTransactionInternal(AssetRequest assetRequest) {
        return transactionService.addTransaction(assetRequest);
    }

    public List<AssetResponse> getStockQuantity(UserMail userMail, String stockCode) {

        String email = userMail.getEmail();

        List<AssetEntity> stockEntities = portfolioRepository.findByEmailAndStockCodeOrderByTransactionDate(email, stockCode);

        if (stockEntities.isEmpty()) {
            throw new IllegalArgumentException("Stock not found");
        }

        return TCollectionUtil.map(stockEntities, asset -> TObjectMapper.copy(asset, AssetResponse.class));
    }

    public List<AssetResponse> getAllStocks(UserMail userMail) {

        List<AssetEntity> stockEntities = portfolioRepository.findByEmail(userMail.getEmail());

        Map<String, List<AssetEntity>> stockEntityMap = stockEntities.stream()
                .collect(Collectors.groupingBy(stockWithCodeAndBroker()));

        List<AssetResponse> responseEntities = TCollectionUtil.map(stockEntityMap.values(),
                PortfolioService::combineAllDetailsOfEntities);

        log.info("Fetching portfolio stocks of {}", userMail.getEmail());
        return responseEntities;
    }

    public List<AssetResponse> getStocksWithDateRange(UserMail userMail, LocalDate startDate, LocalDate endDate) {

        String email = userMail.getEmail();

        List<AssetEntity> stockEntities = portfolioRepository.findByEmailAndTransactionDateBetween(email, startDate, endDate);

        Map<String, List<AssetEntity>> stockEntityMap = stockEntities.stream()
                .collect(Collectors.groupingBy(stockWithCodeAndBroker()));
        List<AssetResponse> responseEntities = TCollectionUtil.map(stockEntityMap.values(),
                PortfolioService::combineAllDetailsOfEntities);

        log.info("Fetching stocks from portfolio between {} and {}", startDate, endDate);
        return responseEntities;
    }

    /**
     * A method that combines all details of stock entities to single entity based
     * on a given stock code.
     *
     * @param stockEntities a list of stock entities to search through
     * @return the response entity that matches the provided stock code
     */
    private static AssetResponse combineAllDetailsOfEntities(List<AssetEntity> stockEntities) {
        AssetResponse assetResponse = TObjectMapper.copy(stockEntities.getFirst(), AssetResponse.class);

        double totalValue = 0;
        double quantity = 0;
        double brokerCharges = 0;
        double miscCharges = 0;
        Map<String, Double> transactionDatesMap = new HashMap<>();

        for (AssetEntity entity : stockEntities) {
            totalValue += entity.getTotalValue();
            quantity += entity.getQuantity();
            brokerCharges += entity.getBrokerCharges();
            miscCharges += entity.getMiscCharges();

            Double existingQuantity = transactionDatesMap.get(TLocaleDate.convertToString(entity.getTransactionDate()));
            Double latestQuantity = entity.getQuantity();
            if (existingQuantity != null) {
                latestQuantity += existingQuantity;
            }
            transactionDatesMap.put(TLocaleDate.convertToString(entity.getTransactionDate()), latestQuantity);
        }

        assetResponse.setQuantity(quantity);
        assetResponse.setTotalQuantity(quantity);
        assetResponse.setTotalValue(totalValue);
        assetResponse.setPrice(totalValue / quantity);
        assetResponse.setBrokerCharges(brokerCharges);
        assetResponse.setMiscCharges(miscCharges);
        Map<String, Double> transactionQuantities = transactionDatesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        assetResponse.setTransactionQuantities(transactionQuantities);
        assetResponse.setTransactionDate(null);
        return assetResponse;
    }

    private static double getTotalValue(AssetRequest assetRequest) {
        return assetRequest.getPrice() * assetRequest.getQuantity();
    }

    private void updateQuantity(UserMail userMail, String transactionId, List<AssetEntity> stockEntities, AssetRequest assetRequest) {

        double sellQuantity = assetRequest.getQuantity();

        Iterator<AssetEntity> stockEntitiesIterator = stockEntities.iterator();
        while (sellQuantity > 0) {

            AssetEntity assetEntity = stockEntitiesIterator.next();
            assetEntity.getSellTransactionIds().add(transactionId);
            Double assetQuantity = assetEntity.getQuantity();

            ReportContext reportContext;
            ProfitAndLossContext profitAndLossContext;

            if (sellQuantity >= assetQuantity) {
                reportContext = toReportContext(assetEntity, assetRequest, assetQuantity);
                profitAndLossContext = ProfitAndLossContext.from(assetEntity, assetRequest, assetQuantity);

                assetEntity.setQuantity(0D);
                assetEntity.setTotalValue(0);
                sellQuantity = sellQuantity - assetQuantity;
            } else {
                reportContext = toReportContext(assetEntity, assetRequest, sellQuantity);
                profitAndLossContext = ProfitAndLossContext.from(assetEntity, assetRequest, sellQuantity);

                double remainingQuantity = assetQuantity - sellQuantity;
                assetEntity.setQuantity(remainingQuantity);
                assetEntity.setTotalValue(remainingQuantity * assetEntity.getPrice());
                sellQuantity = 0;
            }

            reportService.stockReport(userMail, reportContext);
            profitAndLossService.updateProfitAndLoss(userMail, profitAndLossContext);
        }
    }

    private static ReportContext toReportContext(AssetEntity assetEntity, AssetRequest assetRequest, double sellQuantity) {

        ReportContext reportContext = ReportContext.empty();

        // Adding asset details to ReportContext
        reportContext.setStockCode(assetEntity.getStockCode());
        reportContext.setStockName(assetEntity.getStockName());
        reportContext.setExchangeName(assetEntity.getExchangeName());
        reportContext.setBrokerName(assetEntity.getBrokerName());
        reportContext.setTotalValue(assetEntity.getTotalValue());
        reportContext.setAssetType(assetEntity.getAssetType());
        reportContext.setPurchasePrice(assetEntity.getPrice());
        reportContext.setPurchaseDate(assetEntity.getTransactionDate());

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

    public List<AssetEntity> searchAssets(UserMail userMail, List<QueryFilter> queryFilters) {
        return mongoTemplateService.getDocuments(userMail, queryFilters, AssetEntity.class);
    }

    public List<AssetEntity> stocksForCorporateActions(String stockCode, LocalDate recordDate) {
        return portfolioRepository.findByStockCodeAndTransactionDateBefore( stockCode, recordDate);
    }

    public void saveCorporateActionProcessedStocks(List<AssetEntity> stocks) {
        portfolioRepository.saveAll(stocks);
    }

//    public List<Asset> testMethod(UserMail userMail, String stockCode, LocalDate recordDate) {
//
//        return portfolioRepository.findByEmailAndStockCodeAndTransactionDateBefore(userMail.getEmail(), stockCode, recordDate);
//    }

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

        List<AssetEntity> assetEntities = switch (holdingType) {
            case LONG_TERM -> getLongTermHeldAssets(userMail, oneYearBeforeDate);
            case SHORT_TERM -> getShortTermHeldAssets(userMail, oneYearBeforeDate);
        };

        Map<String, List<AssetEntity>> resultedAssetsMap = assetEntities.stream()
                .collect(Collectors.groupingBy(stockWithCodeAndBroker()));
        List<AssetResponse> resultedAssets = TCollectionUtil.map(resultedAssetsMap.values(),
                PortfolioService::combineAllDetailsOfEntities);

        List<String> stockCodes = TCollectionUtil.map(resultedAssets, AssetResponse::getStockCode);
        Map<String, Double> assetQuantityMap = TCollectionUtil.toMap(resultedAssets, stockCodeWithBroker(),
                AssetResponse::getQuantity);

        List<AssetResponse> allAssets = getStockEntities(userMail, new HashSet<>(stockCodes));

        Function<AssetResponse, AssetResponse> quantityUpdater = assetResponse -> {
            double quantity = assetResponse.getQuantity();
            assetResponse.setTotalQuantity(quantity);
            String stockWithBroker = assetResponse.getStockCode() + assetResponse.getBrokerName();
            assetResponse.setQuantity(assetQuantityMap.getOrDefault(stockWithBroker, 0.0));
            return assetResponse;
        };

        return TCollectionUtil.mapAndApply(allAssets, quantityUpdater, assetResponse -> assetResponse.getQuantity() > 0);
    }

    private Function<AssetEntity, String> stockWithCodeAndBroker() {
        return asset -> asset.getStockCode() + asset.getBrokerName().name();
    }

    private Function<AssetResponse, String> stockCodeWithBroker() {
        return assetResponse -> assetResponse.getStockCode() + assetResponse.getBrokerName();
    }

    private List<AssetResponse> getStockEntities(UserMail userMail, Collection<String> stockCodes) {

        List<AssetEntity> stockEntities = portfolioRepository.findByEmailAndStockCodeIn(userMail.getEmail(), stockCodes);

        Map<String, List<AssetEntity>> stockEntityMap = stockEntities.stream()
                .collect(Collectors.groupingBy(stockWithCodeAndBroker()));

        return TCollectionUtil.map(stockEntityMap.values(), PortfolioService::combineAllDetailsOfEntities);
    }

    private List<AssetEntity> getLongTermHeldAssets(UserMail userMail, String oneYearBeforeDate) {

        QueryFilter queryFilter = QueryFilter.builder()
                .filterKey("transaction_date")
                .value(oneYearBeforeDate)
                .operation(QueryFilter.FilterOperation.LESSER_THAN)
                .isDateField(true).build();

        List<QueryFilter> queryFilters = new ArrayList<>();
        queryFilters.add(queryFilter);

        return searchAssets(userMail, queryFilters);
    }

    private List<AssetEntity> getShortTermHeldAssets(UserMail userMail, String oneYearBeforeDate) {

        QueryFilter queryFilter = QueryFilter.builder()
                .filterKey("transaction_date")
                .value(oneYearBeforeDate)
                .operation(QueryFilter.FilterOperation.GREATER_THAN)
                .isDateField(true).build();

        List<QueryFilter> queryFilters = new ArrayList<>();
        queryFilters.add(queryFilter);

        return searchAssets(userMail, queryFilters);
    }

    public List<AssetResponse> getMutualFunds(UserMail userMail) {

        List<AssetEntity> mutualFunds = portfolioRepository.findByEmailAndAssetType(userMail.getEmail(), AssetType.MUTUAL_FUND);

        Map<String, List<AssetEntity>> fundsMap = mutualFunds.stream()
                .collect(Collectors.groupingBy(stockWithCodeAndBroker()));

        List<AssetResponse> responseEntities = TCollectionUtil.map(fundsMap.values(),
                PortfolioService::combineAllDetailsOfEntities);

        log.info("Fetch holding Mutual Funds of {}", userMail.getEmail());
        return responseEntities;
    }

    public void updateTransactions() {

        log.info("Initiated update of all records of user: {}", "userMail.getEmail()");

        List<AssetEntity> all = portfolioRepository.findAll();
        all.forEach(transaction -> {
            AssetType assetType = transaction.getAssetType();
            transaction.setAssetType(assetType == null ? AssetType.MUTUAL_FUND : assetType);
        });
        portfolioRepository.saveAll(all);

        log.info("Updated all portfolio stocks");
        reportService.updateReports();
        log.info("Updated all reports");
        transactionService.updateTransactions();
        log.info("Updated all transactions");
    }

    public Pair<InputStreamResource, String> downloadPortfolioStocks(UserMail userMail) {
        List<AssetResponse> userStocks = getAllStocks(userMail);

        String fileName = ExcelParser.PORTFOLIO_FILE_NAME;
        ByteArrayInputStream inputStream = ExcelBuilder.downloadAssets(userStocks, false);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return Pair.of(resource, fileName);
    }

    public Pair<InputStreamResource, String> downloadTermAssets(UserMail userMail, HoldingType holdingType) {

        List<AssetResponse> assets = this.getAssets(userMail, holdingType);
        String fileName = ExcelParser.HOLDINGS_FILE_NAME;
        ByteArrayInputStream inputStream = ExcelBuilder.downloadAssets(assets, true);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return Pair.of(resource, fileName);
    }

    public ByteArrayInputStream downloadTemplate() {
        return ExcelBuilder.downloadTemplate();
    }
}
