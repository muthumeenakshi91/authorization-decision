package com.javaproject.authorization_decisioning.security;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class JwtSecurityTest {

    @Test
    void shouldRejectRequestWithoutToken() {

        given()
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN6001",
                    "cardNumber": "4111111111111111",
                    "amount": 250.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldRejectRequestWithInvalidToken() {

        given()
            .header(
                "Authorization",
                "Bearer this-is-not-a-valid-jwt"
            )
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN6002",
                    "cardNumber": "4111111111111111",
                    "amount": 250.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldAllowRequestWithValidToken() {

        String token = JwtTestSupport.getToken();

        given()
            .header(
                "Authorization",
                "Bearer " + token
            )
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN6003",
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
            .body("decision", org.hamcrest.Matchers.equalTo("APPROVED"));
    }

    @Test
    void shouldRejectUserWithoutAdminRole() {

        String token =
            given()
                .contentType("application/json")
                .body("""
                    {
                        "username": "user",
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
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
                {
                    "transactionId": "TXN6004",
                    "cardNumber": "4111111111111111",
                    "amount": 250.00,
                    "currency": "CAD",
                    "merchant": "ABC_STORE"
                }
                """)
        .when()
            .post("http://localhost:8080/api/auth/authorize")
        .then()
            .statusCode(403);
    }
}