package au.com.telstra.simcardactivator;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/activate")
public class SimCardController {

    // 硬编码 Actuator URL
    private String actuatorUrl = "http://localhost:8444/actuate";

    @PostMapping
    public ResponseEntity<String> activateSimCard(@RequestBody Map<String, String> requestBody) {

        String iccid = requestBody.get("iccid");
        String customerEmail = requestBody.get("customerEmail");

        if (iccid == null || customerEmail == null) {
            return ResponseEntity.badRequest().body("ICCID and Customer Email are required.");
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();
        payload.put("iccid", iccid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(actuatorUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("success")) {
                boolean success = Boolean.parseBoolean(responseBody.get("success").toString());
                return ResponseEntity.ok("Activation success: " + success);
            }

            return ResponseEntity.status(500).body("Unexpected response from actuator.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error communicating with actuator: " + e.getMessage());
        }
    }
}
