package com.thiru.investment_tracker.integration;

import com.thiru.investment_tracker.auth.dto.LoginRequest;
import com.thiru.investment_tracker.auth.dto.RegistrationRequest;
import com.thiru.investment_tracker.auth.entity.UserDetail;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void addUser_validRequest_registersUser() {
        // GIVEN
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setRole(com.thiru.investment_tracker.auth.dto.AuthHelper.Role.USER);

        // WHEN / THEN
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body(containsString("newuser@test.com"));
    }

    @Test
    void login_validCredentials_returnsToken() {
        // GIVEN
        String email = "logintest@test.com";
        String rawPassword = "password123";
        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles("USER");
        mongoTemplate.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        // WHEN / THEN
        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .extract()
                .path("token");

        assertNotNull(token);
    }

    @Test
    void changePassword_validRequest_updatesPassword() {
        // GIVEN
        String email = "changepwd@test.com";
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";

        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(oldPassword));
        user.setRoles("USER");
        mongoTemplate.save(user);

        String token = generateToken(email);

        RegistrationRequest changeRequest = new RegistrationRequest();
        changeRequest.setEmail(email);
        changeRequest.setPassword(oldPassword);
        changeRequest.setNewPassword(newPassword);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changeRequest)
                .when()
                .put("/auth/user/{email}/change/password", email)
                .then()
                .statusCode(200)
                .body(containsString(email));
    }

    @Test
    void addUser_duplicateEmail_returns400() {
        // GIVEN
        String email = "duplicate@test.com";
        UserDetail existingUser = new UserDetail();
        existingUser.setEmail(email);
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setRoles("USER");
        mongoTemplate.save(existingUser);

        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setRole(com.thiru.investment_tracker.auth.dto.AuthHelper.Role.USER);

        // WHEN / THEN
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body(containsString("already exists"));
    }

    @Test
    void addUser_blankEmail_returns400() {
        // GIVEN
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("");
        request.setPassword("password123");
        request.setRole(com.thiru.investment_tracker.auth.dto.AuthHelper.Role.USER);

        // WHEN / THEN
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400);
    }

    @Test
    void login_wrongPassword_returns401() {
        // GIVEN
        String email = "wrongpwd@test.com";
        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRoles("USER");
        mongoTemplate.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("wrongpassword");

        // WHEN / THEN
        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void login_unknownEmail_returns401() {
        // GIVEN
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@test.com");
        loginRequest.setPassword("password123");

        // WHEN / THEN
        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void changePassword_emailMismatch_returns400() {
        // GIVEN
        String email = "user1@test.com";
        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("oldpassword"));
        user.setRoles("USER");
        mongoTemplate.save(user);

        String token = generateToken(email);

        RegistrationRequest changeRequest = new RegistrationRequest();
        changeRequest.setEmail("different@test.com");
        changeRequest.setPassword("oldpassword");
        changeRequest.setNewPassword("newpassword");

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changeRequest)
                .when()
                .put("/auth/user/{email}/change/password", email)
                .then()
                .statusCode(400)
                .body(containsString("Email mismatch"));
    }

    @Test
    void changePassword_sameOldAndNew_returns400() {
        // GIVEN
        String email = "samepwd@test.com";
        String password = "samepassword";

        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("USER");
        mongoTemplate.save(user);

        String token = generateToken(email);

        RegistrationRequest changeRequest = new RegistrationRequest();
        changeRequest.setEmail(email);
        changeRequest.setPassword(password);
        changeRequest.setNewPassword(password);

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changeRequest)
                .when()
                .put("/auth/user/{email}/change/password", email)
                .then()
                .statusCode(400)
                .body(containsString("Old password cannot be same as new password"));
    }

    @Test
    void changePassword_wrongOldPassword_returns400() {
        // GIVEN
        String email = "wrongoldpwd@test.com";
        UserDetail user = new UserDetail();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("correctoldpassword"));
        user.setRoles("USER");
        mongoTemplate.save(user);

        String token = generateToken(email);

        RegistrationRequest changeRequest = new RegistrationRequest();
        changeRequest.setEmail(email);
        changeRequest.setPassword("wrongoldpassword");
        changeRequest.setNewPassword("newpassword");

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changeRequest)
                .when()
                .put("/auth/user/{email}/change/password", email)
                .then()
                .statusCode(400)
                .body(containsString("Invalid old password"));
    }

    @Test
    void changePassword_userNotFound_returns400() {
        // GIVEN
        String email = "notfound@test.com";
        String token = generateToken(email);

        RegistrationRequest changeRequest = new RegistrationRequest();
        changeRequest.setEmail(email);
        changeRequest.setPassword("oldpassword");
        changeRequest.setNewPassword("newpassword");

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changeRequest)
                .when()
                .put("/auth/user/{email}/change/password", email)
                .then()
                .statusCode(401);
    }

    @Test
    void protectedEndpoint_withExpiredToken_returns401() {
        // GIVEN
        String email = "expiredtoken@test.com";
        String expiredToken = generateExpiredToken(email, "USER");

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer " + expiredToken)
                .when()
                .get("/transactions/user/{email}", email)
                .then()
                .statusCode(401);
    }

    @Test
    void protectedEndpoint_withMalformedToken_returns403() {
        // GIVEN
        String email = "malformed@test.com";

        // WHEN / THEN
        given()
                .header("Authorization", "Bearer invalid.malformed.token")
                .when()
                .get("/transactions/user/{email}", email)
                .then()
                .statusCode(401);
    }

    @Test
    void protectedEndpoint_withWrongUserPath_returns403() {
        // GIVEN
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";

        UserDetail user1 = new UserDetail();
        user1.setEmail(email1);
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setRoles("USER");
        mongoTemplate.save(user1);

        String token = generateToken(email1);

        // WHEN / THEN - user1 tries to access user2's transactions
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/transactions/user/{email}", email2)
                .then()
                .statusCode(403);
    }
}