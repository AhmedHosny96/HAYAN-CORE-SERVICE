package com.hayaan.notification;

import com.hayaan.config.AsyncHttpConfig;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    @Value("${sms.endpoint}")
    private String SMS_ENDPOINT;

    @Value("${sms.auth-url}")
    private String AUTH_URL;

    @Value("${sms.username}")
    private String SMS_USERNAME;

    @Value("${sms.password}")
    private String SMS_PASSWORD;

    private final AsyncHttpConfig asyncHttp;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


    @Async
    public CompletableFuture<Void> sendMail(String toEmail, String subject, String templateName, IContext context) {
        return CompletableFuture.runAsync(() -> {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                String htmlContent = templateEngine.process(templateName, context);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
                log.info("PROBLEM OCCURRED WHILE SENDING EMAIL: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public String generateToken() {

        JSONObject authBody = new JSONObject();
        authBody.put("username", SMS_USERNAME);
        authBody.put("password", SMS_PASSWORD);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(authBody.toString()))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject jsonResponse = new JSONObject(response.body());

        return jsonResponse.optString("token");

    }

    
    public JSONObject sendSms(String phoneNumber, String message) {

        var smsRequest = new JSONObject();
        smsRequest.put("receiverAddress", phoneNumber);
        smsRequest.put("message", message);

        log.info("SMS REQUEST : {}", smsRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(SMS_ENDPOINT)
                .setHeader("Authorization", "Bearer " + generateToken())
                .setBody(smsRequest.toString());

        JSONObject smsResponse = asyncHttp.sendRequest(requestBody);

        log.info("SMS RESPONSE : {}", smsResponse);

        return smsResponse;

    }
}
