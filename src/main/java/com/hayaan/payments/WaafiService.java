package com.hayaan.payments;

import com.hayaan.dto.CustomResponse;
import com.hayaan.flight.object.entity.Payment;
import com.hayaan.flight.object.entity.TicketHistory;
import com.hayaan.flight.repo.PaymentRepository;
import com.hayaan.flight.repo.TicketHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaafiService {

    private final PaymentRepository paymentRepository;
    private final TicketHistoryRepo ticketHistoryRepo;
    private final RestTemplate restTemplate;
    @Value("${ebirr.schemaVersion}")
    private String schemaVersion;
    @Value("${ebirr.channelName}")
    private String channelName;
    @Value("${ebirr.serviceName}")
    private String serviceName;

    @Value("${ebirr.paymentMethod}")
    private String paymentMethod;

    @Value("${waafi.endpoint}")
    private String waafiEndpoint;

    @Value("${waafi.merchantId}")
    private String waafiMerchantId;

    @Value("${waafi.apiUserId}")
    private String waafiApiUserId;

    @Value("${waafi.apiKey}")
    private String waafiApiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    /**
     * Pay with Waafi - Async Payment with Push Notification
     * <p>
     * This method handles the new Waafi async flow:
     * 1. Sends payment request to Waafi
     * 2. Waafi sends push notification to customer
     * 3. Waits for customer approval (polling for up to 30+ seconds)
     * 4. Returns final payment status
     *
     * @param reference Payment reference (PNR)
     * @param mobile    Customer mobile number
     * @return CustomResponse with payment result
     */
    public CustomResponse payWithWaafi(String reference, String mobile) {
        Optional<Payment> paymentOptional = paymentRepository.findByPnr(reference);
        if (paymentOptional.isEmpty()) {
            return new CustomResponse(400, "Pnr not found", null);
        }

        Payment payment = paymentOptional.get();
        Optional<TicketHistory> ticketHistoryOpt = ticketHistoryRepo.findByPnr(reference);

        if (ticketHistoryOpt.isEmpty()) {
            return new CustomResponse(400, "Ticket history not found for PNR: " + reference, null);
        }

        TicketHistory ticketHistory = ticketHistoryOpt.get();

        // Prepare JSON request
        JSONObject request = new JSONObject();
        request.put("schemaVersion", schemaVersion);
        request.put("requestId", UUID.randomUUID().toString());
        request.put("timestamp", Instant.now().toString());
        request.put("channelName", channelName);
        request.put("serviceName", serviceName);

        JSONObject serviceParams = new JSONObject();
        serviceParams.put("merchantUid", waafiMerchantId);
        serviceParams.put("apiUserId", waafiApiUserId);
        serviceParams.put("apiKey", waafiApiKey);
        serviceParams.put("paymentMethod", paymentMethod);

        JSONObject payerInfo = new JSONObject();
        payerInfo.put("accountNo", mobile);
        serviceParams.put("payerInfo", payerInfo);

        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("referenceId", reference);
        transactionInfo.put("invoiceId", reference);
        transactionInfo.put("amount", String.valueOf(payment.getAmount()));
        transactionInfo.put("currency", "USD");
        transactionInfo.put("description", "Flight payment for " + reference);
        serviceParams.put("transactionInfo", transactionInfo);

        request.put("serviceParams", serviceParams);

        log.info("Waafi Payment request: {}", request.toString(2));

        // Build HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // ✅ ensure JSON is expected
        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        try {
//            RestTemplate waafiRestTemplate = createWaafiRestTemplate();
            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(waafiEndpoint, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - start;

            String responseBody = response.getBody();
            log.info("Waafi response received in {}ms: {}", duration, responseBody);

            // Check if valid JSON
            if (responseBody == null || !responseBody.trim().startsWith("{")) {
                log.error("Waafi returned non-JSON response: {}", responseBody);
                return new CustomResponse(500, "Invalid response from Waafi", null);
            }

            JSONObject responseJson = new JSONObject(responseBody);
            String responseCode = responseJson.optString("responseCode");
            String message = responseJson.optString("responseMsg", "Unknown response");

            // ✅ Success case
            if ("2001".equals(responseCode)) {
                JSONObject params = responseJson.optJSONObject("params");
                String transactionId = params != null ? params.optString("transactionId", null) : null;

                payment.setPayerAccount(mobile);
                payment.setPaymentReference(transactionId);
                payment.setPaymentStatus(2);
                payment.setPaymentStatusDesc("COMPLETED");
                payment.setResponseBody(params != null ? params.toString() : null);
                payment.setResponse("000");
                payment.setPaymentMode("WAAFI");
                paymentRepository.save(payment);

                ticketHistory.setPaymentReference(transactionId);
                ticketHistory.setStatus(1);
                ticketHistoryRepo.save(ticketHistory);

                return new CustomResponse(200, message, transactionId);
            }

            // ❌ Failure case
            String errorDesc = responseJson.optString("description", message);
            log.warn("Waafi payment failed: {}", errorDesc);
            return new CustomResponse(400, errorDesc, null);

        } catch (Exception e) {
            log.error("Error calling Waafi payment API", e);
            return new CustomResponse(500, "Unable to process payment: " + e.getMessage(), null);
        }
    }


//    private RestTemplate createWaafiRestTemplate() {
//        int timeout = 35000;
//
//        CloseableHttpClient httpClient = HttpClientBuilder.create()
//                .setRedirectStrategy(new LaxRedirectStrategy()) // handles redirects
//                .build();
//
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        factory.setConnectTimeout(5000);
//        factory.setReadTimeout(timeout);
//
//        return new RestTemplate(factory);
//    }


}
