Feature: SIM Card Activation

  Scenario: Successful SIM card activation
    Given the ICCID is "1255789453849037777" and the customer email is "test@example.com"
    When I send a POST request to /activate
    Then the activation result at ID 1 should be "true"

  Scenario: Failed SIM card activation
    Given the ICCID is "8944500102198304826" and the customer email is "fail@example.com"
    When I send a POST request to /activate
    Then the activation result at ID 2 should be "false"