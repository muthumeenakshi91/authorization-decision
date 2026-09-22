package com.javaproject.authorization_decisioning.ui.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

public class AuthorizationPage {

    private WebDriver driver;

    private By transactionId =
            By.id("transactionId");

    private By cardNumber =
            By.id("cardNumber");

    private By amount =
            By.id("amount");

    private By currency =
            By.id("currency");

    private By merchant =
            By.id("merchant");

    private By authorizeButton =
            By.id("authorizeButton");

    private By decision =
            By.id("decision");

    private By reason =
            By.id("reason");

    public AuthorizationPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterTransactionId(String value) {
        driver.findElement(transactionId)
                .sendKeys(value);
    }

    public void enterCardNumber(String value) {
        driver.findElement(cardNumber)
                .sendKeys(value);
    }

    public void enterAmount(String value) {
        driver.findElement(amount)
                .sendKeys(value);
    }

    public void selectCurrency(String value) {

        Select currencyDropdown =
                new Select(
                        driver.findElement(currency)
                );

        currencyDropdown.selectByValue(value);
    }
    
    public void enterMerchant(String value) {
        driver.findElement(merchant)
                .sendKeys(value);
    }

    public void clickAuthorize() {
        driver.findElement(authorizeButton)
                .click();
    }

    public String getDecision() {

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(10)
                );

        wait.until(driver -> {
                String text =
                        driver.findElement(decision)
                                .getText();

                return text.equals("APPROVED")
                        || text.equals("DECLINED")
                        || text.equals("ERROR");
        });

        return driver.findElement(decision)
                .getText();
    }

    public String getReason() {
        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(10)
                );
        
        wait.until(driver -> {
                String text =
                        driver.findElement(reason)
                                .getText();

                return text != "Request failed";
        });

        return driver.findElement(reason)
                .getText();
    }

    public void enterAuthorizationDetails(
                String transactionId,
                String cardNumber,
                String amount,
                String currency,
                String merchant) {

        enterTransactionId(transactionId);
        enterCardNumber(cardNumber);
        enterAmount(amount);
        selectCurrency(currency);
        enterMerchant(merchant);
    }

    public boolean isDecisionDisplayed(String expectedDecision) {
        return getDecision().equals(expectedDecision);
    }
}