package com.javaproject.authorization_decisioning.api;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeAll;
import com.javaproject.authorization_decisioning.security.JwtTestSupport;

class AuthorizationApiTest {

    private static String token;

    @BeforeAll
    static void setupAuthentication() {
        token = JwtTestSupport.getToken();
    }

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }
    @Test
    void shouldApproveValidTransaction() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN1002",
                    "cardNumber": "4111111111111111",
                    "amount": 250.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN1002"))
            .body("decision", equalTo("APPROVED"))
            .body("reason", equalTo("Transaction approved"));
    }

    @Test
    void shouldDeclineTransactionWhenAmountExceedsLimit() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN2002",
                    "cardNumber": "4111111111111111",
                    "amount": 6000.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN2002"))
            .body("decision", equalTo("DECLINED"))
            .body("reason",
                equalTo("Transaction amount exceeds authorization limit"));
    }

    @Test
    void shouldDeclineUnsupportedCurrency() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN2003",
                    "cardNumber": "4111111111111111",
                    "amount": 250.00,
                    "currency": "INR",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN2003"))
            .body("decision", equalTo("DECLINED"))
            .body("reason", equalTo("Unsupported currency"));
    }

    @Test
    void shouldDeclineDuplicateTransaction() {

        String requestBody = """
            {
                "transactionId": "TXN2004",
                "cardNumber": "4111111111111111",
                "amount": 250.00,
                "currency": "CAD",
                "merchant": "ABC_STORE"
            }
            """;

        // First request
        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(200)
            .body("decision", equalTo("APPROVED"));

        // Second request with the same transaction ID
        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN2004"))
            .body("decision", equalTo("DECLINED"))
            .body("reason", equalTo("Duplicate transaction"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCardNumber() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN2005",
                    "cardNumber": "12345",
                    "amount": 250.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsMissing() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN2006",
                    "cardNumber": "4111111111111111",
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsZero() {

        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN2007",
                    "cardNumber": "4111111111111111",
                    "amount": 0,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturnTransactionWhenFound() {

        String requestBody = """
            {
                "transactionId": "TXN2008",
                "cardNumber": "4111111111111111",
                "amount": 250.00,
                "currency": "CAD",
                "merchant": "ABC_STORE"
            }
            """;

        // Create transaction first
        given()
            .header("Authorization","Bearer "+token)
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/api/auth/authorize")
        .then()
            .statusCode(200);

        // Retrieve transaction
        given()
            .header("Authorization","Bearer "+token)
        .when()
            .get("/api/auth/TXN2008")
        .then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN2008"))
            .body("decision", equalTo("APPROVED"))
            .body("reason", equalTo("Transaction approved"))
            .body("currency", equalTo("CAD"))
            .body("merchant", equalTo("ABC_STORE"));
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() {

        System.out.println("TOKEN = " + token);
        
        given()
            .header("Authorization","Bearer "+token)
        .when()
            .get("http://localhost:8080/api/auth/TXN9999")
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("error", equalTo("NOT_FOUND"))
            .body("message", equalTo("Transaction not found: TXN9999"));
    }
}