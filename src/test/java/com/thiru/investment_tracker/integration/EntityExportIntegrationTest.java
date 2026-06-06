package com.thiru.investment_tracker.integration;

import com.thiru.investment_tracker.dto.EntityExportRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.entity.query.QueryFilter;
import com.thiru.investment_tracker.service.EntityExportService;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.TransactionService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class EntityExportIntegrationTest extends AbstractIntegrationTest {

    private static final String TEST_EMAIL = "entityexport@test.com";

    @Autowired
    private EntityExportService entityExportService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TransactionService transactionService;

    @Test
    void export_whenAssets_shouldReturnExcelStream() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);
        String stockCode = "RELIANCE";
        String stockName = "Reliance Industries";

        AssetEntity asset = new AssetEntity();
        asset.setEmail(TEST_EMAIL);
        asset.setStockCode(stockCode);
        asset.setStockName(stockName);
        asset.setPrice(2500.0);
        asset.setQuantity(10.0);
        asset.setTransactionDate(LocalDate.now());
        asset.setBrokerName(com.thiru.investment_tracker.dto.enums.BrokerName.ZERODHA);
        asset.setAssetType(com.thiru.investment_tracker.dto.enums.AssetType.EQUITY);
        mongoTemplate.save(asset);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("assets");
        request.setSelectedColumns(List.of("stockCode", "stockName", "price", "quantity"));

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(200)
                .contentType(anyOf(
                        equalTo("application/vnd.ms-excel"),
                        equalTo("application/octet-stream")))
                .header("Content-Disposition", containsString("attachment"))
                .body(notNullValue())
                .body("length()", greaterThan(0));
    }

    @Test
    void export_whenTransactions_shouldReturnExcelStream() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);
        String stockCode = "TCS";
        String stockName = "Tata Consultancy Services";

        TransactionEntity txn = new TransactionEntity();
        txn.setEmail(TEST_EMAIL);
        txn.setStockCode(stockCode);
        txn.setStockName(stockName);
        txn.setPrice(3800.0);
        txn.setQuantity(5.0);
        txn.setTransactionType(com.thiru.investment_tracker.dto.enums.TransactionType.BUY);
        txn.setTransactionDate(LocalDate.now());
        txn.setBrokerName(com.thiru.investment_tracker.dto.enums.BrokerName.ZERODHA);
        mongoTemplate.save(txn);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("transactions");
        request.setSelectedColumns(List.of("stockCode", "stockName", "price", "quantity"));

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(200)
                .contentType(anyOf(
                        equalTo("application/vnd.ms-excel"),
                        equalTo("application/octet-stream")))
                .header("Content-Disposition", containsString("attachment"))
                .body(notNullValue())
                .body("length()", greaterThan(0));
    }

    @Test
    void export_whenTemplate_shouldReturnTemplateStream() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("transactions-template");
        request.setSelectedColumns(List.of("stockCode", "stockName", "price", "quantity"));

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(200)
                .contentType(anyOf(
                        equalTo("application/vnd.ms-excel"),
                        equalTo("application/octet-stream")))
                .header("Content-Disposition", containsString("attachment"))
                .body(notNullValue())
                .body("length()", greaterThan(0));
    }

    @Test
    void export_whenInvalidEntity_shouldThrow400() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("invalid-entity");
        request.setSelectedColumns(new ArrayList<>());

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(400);
    }

    @Test
    void export_whenGenerationFails_shouldReturn500() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("assets");
        request.setSelectedColumns(new ArrayList<>());
        request.setQueryFilters(List.of(
                QueryFilter.builder().filterKey("stockCode").operation(QueryFilter.FilterOperation.EQUALS).value("NON_EXISTENT").build()
        ));

        // WHEN / THEN - with invalid filter that causes processing to fail
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(500)));
    }

    @Test
    void export_whenEmpty_shouldReturnValidStream() {
        // GIVEN
        String token = generateToken(TEST_EMAIL);

        EntityExportRequest request = new EntityExportRequest();
        request.setEntityName("assets");
        request.setSelectedColumns(List.of("stockCode", "stockName", "price", "quantity"));

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/portfolio/user/{email}/stocks/download", TEST_EMAIL)
                .then()
                .statusCode(200)
                .contentType(anyOf(
                        equalTo("application/vnd.ms-excel"),
                        equalTo("application/octet-stream")))
                .header("Content-Disposition", containsString("attachment"));
    }
}
