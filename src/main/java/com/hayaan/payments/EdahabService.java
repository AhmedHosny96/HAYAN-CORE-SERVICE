package com.hayaan.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdahabService {


    private final TicketHistoryRepo ticketHistoryRepo;
    @Value("${edahab.agentCode}")
    private String AGENT_CODE;
    @Value("${edahab.apiKey}")
    private String API_KEY;
    @Value("${edahab.apiSecret}")
    private String API_SECRET;
    @Value("${edahab.apiUrl}")
    private String API_URL;

    private final RestTemplate restTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private final PaymentRepository paymentRepository;


    public String generateHash(String requestJson) {
        try {
            String toHash = requestJson + API_SECRET;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    public CustomResponse createInvoice(String pnr, String currency, String phoneNumber) {

        if (currency == null || (!currency.equals("USD") && !currency.equals("SLSH"))) {
            return new CustomResponse(400, "Only USD and SLSH are accepted currencies in edahab payment", null);
        }

        Optional<Payment> paymentOptional = paymentRepository.findByPnr(pnr);

        if (!paymentOptional.isPresent()) {
            return new CustomResponse(400, "Pnr not found", null);
        }

        Payment payment = paymentOptional.get();

        TicketHistory ticketHistory = ticketHistoryRepo.findByPnr(pnr).get();

        try {
            // Step 1: Prepare request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("apiKey", API_KEY);
            requestBody.put("edahabNumber", phoneNumber);
            requestBody.put("amount", payment.getAmount()); // optionally use payment.getAmount() if dynamic
            requestBody.put("agentCode", AGENT_CODE);
            requestBody.put("currency", currency);

            // Step 2: Generate hash from actual request body
            String requestJson = requestBody.toString(); // compact JSON
            String hash = generateHash(requestJson);     // correct hash input

            // Step 3: Setup HTTP request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            String url = API_URL + "/issueinvoice?hash=" + hash;

            log.info("Request body: {}", requestJson);
            log.info("Hash input: {}", requestJson + API_SECRET);
            log.info("Hash URL: {}", url);

            // Step 4: Send request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("API response: {}", response.getBody());

            // Step 5: Handle response
            JSONObject jsonResponse = new JSONObject(response.getBody());
            int statusCode = jsonResponse.optInt("StatusCode");
            String statusDescription = jsonResponse.optString("StatusDescription", "Unknown");

            if (statusCode != 0) {
                return new CustomResponse(400, statusDescription, null);
            }
            // pay ticket here

            String paymentReference = jsonResponse.optString("TransactionId");

            payment.setPayerAccount(phoneNumber);
            payment.setPaymentReference(paymentReference);
            payment.setPaymentStatus(2);
            payment.setPaymentStatusDesc("COMPLETED");
            payment.setResponseBody(jsonResponse.toString());
            payment.setResponse("000");
            payment.setPaymentMode("EDAHAB");
            paymentRepository.save(payment);

            ticketHistory.setPaymentReference(paymentReference);
            ticketHistory.setStatus(1);

            ticketHistoryRepo.save(ticketHistory);

            return new CustomResponse(200, statusDescription, null);

        } catch (Exception e) {
            throw new RuntimeException("eDahab invoice creation failed", e);
        }
    }


}
