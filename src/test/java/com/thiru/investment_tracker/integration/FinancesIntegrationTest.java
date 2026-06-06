package com.thiru.investment_tracker.integration;

import com.thiru.investment_tracker.dto.FinanceRequest;
import com.thiru.investment_tracker.dto.FinanceResponse;
import com.thiru.investment_tracker.dto.enums.CalculationType;
import com.thiru.investment_tracker.service.FinancesService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class FinancesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FinancesService financesService;

    @Test
    void calculate_whenEmi_shouldReturnEmi() {
        // GIVEN
        String token = generateToken("finance@test.com");

        FinanceRequest request = new FinanceRequest();
        request.setCalculationType(CalculationType.EMI);
        request.setPrincipalAmount(100000.0);
        request.setRateOfInterest(12.0);
        request.setTimePeriod(12);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/finances/calculate")
                .then()
                .statusCode(200)
                .body("emiAmount", greaterThan(0))
                .body(notNullValue());
    }

    @Test
    void calculate_whenTotalAmount_shouldReturnTotal() {
        // GIVEN
        String token = generateToken("finance@test.com");

        FinanceRequest request = new FinanceRequest();
        request.setCalculationType(CalculationType.TOTAL_AMOUNT);
        request.setPrincipalAmount(50000.0);
        request.setRateOfInterest(10.0);
        request.setTimePeriod(6);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/finances/calculate")
                .then()
                .statusCode(200)
                .body("finalAmount", greaterThan(0.0))
                .body(notNullValue());
    }

    @Test
    void calculate_whenRateOfInterest_shouldPassthrough() {
        // GIVEN
        String token = generateToken("finance@test.com");

        FinanceRequest request = new FinanceRequest();
        request.setCalculationType(CalculationType.RATE_OF_INTEREST);
        request.setRateOfInterest(8.5);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/finances/calculate")
                .then()
                .statusCode(200)
                .body("rateOfInterest", equalTo(8.5f));
    }

    @Test
    void calculate_whenNone_shouldThrow400() {
        // GIVEN
        String token = generateToken("finance@test.com");

        FinanceRequest request = new FinanceRequest();
        request.setCalculationType(CalculationType.NONE);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/finances/calculate")
                .then()
                .statusCode(400);
    }

    @Test
    void calculate_whenZeroPrincipal_shouldReturnZero() {
        // GIVEN
        String token = generateToken("finance@test.com");

        FinanceRequest request = new FinanceRequest();
        request.setCalculationType(CalculationType.EMI);
        request.setPrincipalAmount(0.0);
        request.setRateOfInterest(12.0);
        request.setTimePeriod(12);

        // WHEN
        FinanceResponse response = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/finances/calculate")
                .then()
                .statusCode(200)
                .extract()
                .as(FinanceResponse.class);

        // THEN - EMI with 0 principal should be 0
        assertEquals(0, response.getEmiAmount());
    }
}
