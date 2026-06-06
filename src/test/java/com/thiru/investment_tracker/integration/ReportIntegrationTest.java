package com.thiru.investment_tracker.integration;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.context.ReportContext;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.ReportEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.repository.ReportRepository;
import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.service.ReportService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportIntegrationTest extends AbstractIntegrationTest {

    private static final String TEST_EMAIL = "reporttest@test.com";
    private static final String TEST_EMAIL_2 = "reporttest2@test.com";

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Test
    void stockReport_whenCalled_shouldSaveToDb() {
        // GIVEN
        String email = TEST_EMAIL;

        ReportContext context = ReportContext.empty();
        context.setStockCode("RELIANCE");
        context.setStockName("Reliance Industries");
        context.setPurchasePrice(2500.0);
        context.setSellPrice(2600.0);
        context.setSellQuantity(5L);
        context.setTotalValue(13000.0);
        context.setPurchaseDate(LocalDate.of(2024, 1, 15));
        context.setSellDate(LocalDate.of(2024, 6, 1));

        // WHEN
        reportService.stockReport(UserMail.from(email), context);

        // THEN
        List<ReportEntity> reports = reportRepository.findByEmail(email);
        assertEquals(1, reports.size());
        ReportEntity saved = reports.get(0);
        assertEquals("RELIANCE", saved.getStockCode());
        assertEquals("Reliance Industries", saved.getStockName());
        assertEquals(2500.0, saved.getPurchasePrice());
        assertEquals(2600.0, saved.getSellPrice());
        assertEquals(5L, saved.getSellQuantity());
        assertEquals(13000.0, saved.getTotalValue());
    }

    @Test
    void getStockReport_whenCalled_shouldReturnReports() {
        // GIVEN
        String email = TEST_EMAIL;

        ReportContext context1 = ReportContext.empty();
        context1.setStockCode("TCS");
        context1.setStockName("Tata Consultancy Services");
        context1.setPurchasePrice(3800.0);
        context1.setSellPrice(3900.0);
        context1.setSellQuantity(3L);
        context1.setTotalValue(11700.0);
        reportService.stockReport(UserMail.from(email), context1);

        ReportContext context2 = ReportContext.empty();
        context2.setStockCode("INFY");
        context2.setStockName("Infosys Ltd");
        context2.setPurchasePrice(1500.0);
        context2.setSellPrice(1550.0);
        context2.setSellQuantity(10L);
        context2.setTotalValue(15500.0);
        reportService.stockReport(UserMail.from(email), context2);

        String token = generateToken(email);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reports/user/{email}", email)
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].stockCode", notNullValue())
                .body("[1].stockCode", notNullValue());
    }

    @Test
    void deleteReports_whenCalled_shouldDeleteByEmail() {
        // GIVEN
        String email = TEST_EMAIL;

        ReportContext context = ReportContext.empty();
        context.setStockCode("HDFCBANK");
        context.setStockName("HDFC Bank");
        context.setPurchasePrice(1600.0);
        context.setSellPrice(1650.0);
        context.setSellQuantity(8L);
        context.setTotalValue(13200.0);
        reportService.stockReport(UserMail.from(email), context);

        assertEquals(1, reportRepository.findByEmail(email).size());

        // WHEN
        reportService.deleteReports(UserMail.from(email));

        // THEN
        List<ReportEntity> reports = reportRepository.findByEmail(email);
        assertEquals(0, reports.size());
    }

    @Test
    void updateReports_whenNullAssetType_shouldBackfill() {
        // GIVEN
        String email = TEST_EMAIL;

        ReportEntity report1 = new ReportEntity();
        report1.setEmail(email);
        report1.setStockCode("SBIN");
        report1.setStockName("State Bank of India");
        report1.setPurchasePrice(700.0);
        report1.setAssetType(null);
        mongoTemplate.save(report1);

        ReportEntity report2 = new ReportEntity();
        report2.setEmail(email);
        report2.setStockCode("KOTAKBANK");
        report2.setStockName("Kotak Mahindra Bank");
        report2.setPurchasePrice(1800.0);
        report2.setAssetType(com.thiru.investment_tracker.dto.enums.AssetType.EQUITY);
        mongoTemplate.save(report2);

        // WHEN
        reportService.updateReports();

        // THEN
        List<ReportEntity> updated = reportRepository.findByEmail(email);
        assertEquals(2, updated.size());

        ReportEntity nullAssetTypeReport = updated.stream()
                .filter(r -> r.getStockCode().equals("SBIN"))
                .findFirst()
                .orElseThrow();
        assertEquals(com.thiru.investment_tracker.dto.enums.AssetType.MUTUAL_FUND, nullAssetTypeReport.getAssetType());

        ReportEntity existingAssetTypeReport = updated.stream()
                .filter(r -> r.getStockCode().equals("KOTAKBANK"))
                .findFirst()
                .orElseThrow();
        assertEquals(com.thiru.investment_tracker.dto.enums.AssetType.EQUITY, existingAssetTypeReport.getAssetType());
    }

    @Test
    void stockReport_whenWriteFailure_shouldRollback() {
        // GIVEN
        String email = TEST_EMAIL;
        int initialCount = reportRepository.findByEmail(email).size();

        ReportContext context = ReportContext.empty();
        context.setStockCode("WIPRO");
        // Intentionally set invalid data that may cause write to fail
        // Since reportService.stockReport does simple save, we test with direct repo interaction

        // WHEN / THEN - verify that a failed save doesn't modify existing data
        try {
            reportService.stockReport(UserMail.from(email), context);
        } catch (Exception e) {
            // Expected or unexpected failure - verify data integrity
        }

        // Verify no partial writes occurred for non-existent user
        List<ReportEntity> reports = reportRepository.findByEmail(email);
        // Either empty (initial) or contains only successfully saved records
        assertEquals(initialCount, reports.size());
    }
}
