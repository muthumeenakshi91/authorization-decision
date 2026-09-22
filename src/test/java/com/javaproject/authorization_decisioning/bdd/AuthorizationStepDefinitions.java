package com.javaproject.authorization_decisioning.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorizationStepDefinitions {

    private String requestBody;
    private Response response;

    @Given("a valid authorization request")
    public void aValidAuthorizationRequest() {

        requestBody = """
            {
                "transactionId": "CUC1001",
                "cardNumber": "4111111111111111",
                "amount": 250.00,
                "currency": "CAD",
                "merchant": "ABC_STORE"
            }
            """;
    }

    @Given("an authorization request with amount {int}")
    public void authorizationRequestWithAmount(int amount) {

        requestBody = """
            {
                "transactionId": "CUC1002",
                "cardNumber": "4111111111111111",
                "amount": %d,
                "currency": "CAD",
                "merchant": "ABC_STORE"
            }
            """.formatted(amount);
    }

    @Given("an authorization request with an invalid card number")
    public void authorizationRequestWithInvalidCardNumber() {

        requestBody = """
            {
                "transactionId": "CUC1003",
                "cardNumber": "12345",
                "amount": 250.00,
                "currency": "CAD",
                "merchant": "ABC_STORE"
            }
            """;
    }

    @When("I submit the authorization request")
    public void submitAuthorizationRequest() {

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

        response =
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(requestBody)
            .when()
                .post("http://localhost:8080/api/auth/authorize");
    }

    @Then("the transaction should be approved")
    public void transactionShouldBeApproved() {

        assertEquals(200, response.statusCode());
        assertEquals(
            "APPROVED",
            response.jsonPath().getString("decision")
        );
    }

    @Then("the transaction should be declined")
    public void transactionShouldBeDeclined() {

        assertEquals(200, response.statusCode());
        assertEquals(
            "DECLINED",
            response.jsonPath().getString("decision")
        );
    }

    @Then("the request should be rejected with status {int}")
    public void requestShouldBeRejectedWithStatus(int status) {

        assertEquals(status, response.statusCode());
    }
}