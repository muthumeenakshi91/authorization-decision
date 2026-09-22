Feature: Authorization Decisioning

  Scenario: Approve a valid authorization transaction
    Given a valid authorization request
    When I submit the authorization request
    Then the transaction should be approved

  Scenario: Decline a transaction above the authorization limit
    Given an authorization request with amount 6000
    When I submit the authorization request
    Then the transaction should be declined

  Scenario: Reject an authorization request with an invalid card
    Given an authorization request with an invalid card number
    When I submit the authorization request
    Then the request should be rejected with status 400