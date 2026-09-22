package com.javaproject.authorization_decisioning.ui;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;

import com.javaproject.authorization_decisioning.ui.pages.AuthorizationPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class AuthorizationUiTest {

    private WebDriver driver;
    private AuthorizationPage authorizationPage;

    @BeforeEach
    void setUp() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);

        driver.get("http://localhost:8080/");

        authorizationPage =
                new AuthorizationPage(driver);
    }


    @AfterEach
    void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldApproveValidTransaction() {

        authorizationPage.enterAuthorizationDetails(
                "TXN4001",
                "4111111111111111",
                "250",
                "CAD",
                "ABC_STORE"
        );

        authorizationPage.clickAuthorize();

        assertTrue(
                authorizationPage.isDecisionDisplayed("APPROVED")
        );

        assertEquals(
                "Transaction approved",
                authorizationPage.getReason()
        );
    }


    @Test
    void shouldDeclineTransactionWhenAmountExceedsLimit() {

        authorizationPage.enterAuthorizationDetails(
                "TXN4002",
                "4111111111111111",
                "6000",
                "CAD",
                "ABC_STORE"
        );

        authorizationPage.clickAuthorize();

        String decision =
                authorizationPage.getDecision();

        String reason =
                authorizationPage.getReason();
        
        assertEquals("DECLINED", decision);

        assertEquals(
                "Transaction amount exceeds authorization limit",
                reason
        );
    }

    @Test
    void shouldDisplayErrorForInvalidCardNumber() {

        authorizationPage.enterAuthorizationDetails(
                "TXN4003",
                "12345",
                "250",
                "CAD",
                "ABC_STORE"
        );

        authorizationPage.clickAuthorize();

        String decision =
                authorizationPage.getDecision();

        String reason =
                authorizationPage.getReason();

        assertEquals("ERROR", decision);

        assertEquals(
                "Card number must contain exactly 16 digits",
                reason
        );
    }

    @Test
    void shouldPersistApprovedTransactionAfterUiAuthorization() {

        String transactionId = "TXN4004";

        authorizationPage.enterAuthorizationDetails(
                transactionId,
                "4111111111111111",
                "250",
                "CAD",
                "ABC_STORE"
        );

        authorizationPage.clickAuthorize();

        assertTrue(
                authorizationPage.isDecisionDisplayed("APPROVED")
        );

        assertEquals(
                "Transaction approved",
                authorizationPage.getReason()
        );

        String token =
        given()
            .contentType("application/json")
            .body("""
                {
                    "username": "admin",
                    "password": "password"
                }
                """)
        .when()
            .post("http://localhost:8080/api/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        given()
        .header("Authorization", "Bearer "+token)
        .when()
                .get("http://localhost:8080/api/auth/" + transactionId)
        .then()
                .statusCode(200)
                .body("transactionId", equalTo(transactionId))
                .body("decision", equalTo("APPROVED"))
                .body("currency", equalTo("CAD"))
                .body("merchant", equalTo("ABC_STORE"));
    }
}