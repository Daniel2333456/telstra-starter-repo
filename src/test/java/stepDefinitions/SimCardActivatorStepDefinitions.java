package stepDefinitions;

import au.com.telstra.simcardactivator.SimCardActivator; // 显式导入主类
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = SimCardActivator.class // ✅ 显式指定 Spring Boot 配置类
)
public class SimCardActivatorStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    private String iccid;
    private String customerEmail;
    private ResponseEntity<String> postResponse;
    private ResponseEntity<Map> getResponse;

    @Given("the ICCID is {string} and the customer email is {string}")
    public void the_iccid_and_email(String iccid, String email) {
        this.iccid = iccid;
        this.customerEmail = email;
    }

    @When("I send a POST request to \\/activate")
    public void send_post_request_to_activate() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("iccid", iccid);
        requestBody.put("customerEmail", customerEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        postResponse = restTemplate.postForEntity("/activate", entity, String.class);
        Assertions.assertEquals(HttpStatus.OK, postResponse.getStatusCode());
    }

    @Then("the activation result at ID {int} should be {string}")
    public void verify_activation_result(int id, String expectedActive) {
        getResponse = restTemplate.getForEntity("/simCard/" + id, Map.class);
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) getResponse.getBody();
        Assertions.assertNotNull(responseBody);

        String actual = responseBody.get("active").toString();
        Assertions.assertEquals(expectedActive, actual);
    }
}
