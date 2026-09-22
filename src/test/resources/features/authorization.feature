Feature: Authorization Decisioning

  Scenario: Approve a valid authorization transaction
    Given a valid authorization request
    When I submit the authorization request
    Then the transaction should be approved