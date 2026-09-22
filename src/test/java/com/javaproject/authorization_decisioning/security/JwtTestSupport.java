package com.javaproject.authorization_decisioning.security;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class JwtTestSupport {

    public static String getToken() {

        Response response =
                given()
                    .contentType("application/json")
                    .body("""
                        {
                            "username": "admin",
                            "password": "password"
                        }
                        """)
                .when()
                    .post("http://localhost:8080/api/login");

        return response
                .then()
                    .statusCode(200)
                    .extract()
                    .path("token");
    }
}