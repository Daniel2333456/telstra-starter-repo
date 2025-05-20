package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class SimCardController {

    private final String actuatorUrl = "http://localhost:8444/actuate";

    @Autowired
    private SimCardRepository simCardRepository;

    // POST /activate
    @PostMapping("/activate")
    public ResponseEntity<String> activateSimCard(@RequestBody Map<String, String> requestBody) {
        String iccid = requestBody.get("iccid");
        String customerEmail = requestBody.get("customerEmail");

        if (iccid == null || customerEmail == null) {
            return ResponseEntity.badRequest().body("ICCID and Customer Email are required.");
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();
        payload.put("iccid", iccid);
        payload.put("customerEmail", customerEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(actuatorUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            boolean success = false;
            if (responseBody != null && responseBody.containsKey("success")) {
                success = Boolean.parseBoolean(responseBody.get("success").toString());
            }

            SimCardRecord record = new SimCardRecord(iccid, customerEmail, success);
            simCardRepository.save(record);

            return ResponseEntity.ok("Activation success: " + success);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error communicating with actuator: " + e.getMessage());
        }
    }

    // GET /simCard/{id}
    @GetMapping("/simCard/{id}")
    public ResponseEntity<?> getSimCardById(@PathVariable("id") Long simCardId) {
        Optional<SimCardRecord> optional = simCardRepository.findById(simCardId);
        if (optional.isPresent()) {
            SimCardRecord record = optional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("iccid", record.getIccid());
            response.put("customerEmail", record.getCustomerEmail());
            response.put("active", record.isActive());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body("SIM Card record not found");
        }
    }
}
