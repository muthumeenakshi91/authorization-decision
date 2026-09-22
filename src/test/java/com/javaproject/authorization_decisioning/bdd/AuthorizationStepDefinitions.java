package com.javaproject.authorization_decisioning.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import io.restassured.response.Response;

public class AuthorizationStepDefinitions {

    private String requestBody;
    private Response response;

    @Given("a valid authorization request")
    public void aValidAuthorizationRequest() {

        requestBody = """
        {
            "transactionId": "TXN3002",
            "cardNumber": "4111111111111111",
            "amount": 250.00,
            "currency": "CAD",
            "merchant": "ABC_STORE"
        }
        """;
    }

    @When("I submit the authorization request")
    public void iSubmitTheAuthorizationRequest() {

        response =
        given()
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post("/api/auth/authorize");
    }

    @Then("the transaction should be approved")
    public void theTransactionShouldBeApproved() {

        response.then()
            .statusCode(200)
            .body("transactionId", equalTo("TXN3001"))
            .body("decision", equalTo("APPROVED"))
            .body("reason", equalTo("Transaction approved"));
    }
}