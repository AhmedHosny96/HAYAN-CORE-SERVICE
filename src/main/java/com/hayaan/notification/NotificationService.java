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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    @Value("${sms.endpoint}")
    private String SMS_ENDPOINT;

    @Value("${sms.username}")
    private String SMS_USERNAME;

    @Value("${sms.password}")
    private String SMS_PASSWORD;

    private final AsyncHttpConfig asyncHttp;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    public void sendMail(String toEmail, String subject, String templateName, IContext context) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        try {
            helper.setTo(toEmail);
            helper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Handle exception
        }

    }


    public JSONObject sendSms(String phoneNumber, String message) {

        var smsRequest = new JSONObject();
        smsRequest.put("username", SMS_USERNAME);
        smsRequest.put("password", SMS_PASSWORD);
        smsRequest.put("receiverAddress", phoneNumber);
        smsRequest.put("message", message);

        log.info("SMS REQUEST : {}", smsRequest);

        RequestBuilder requestBody = new RequestBuilder("POST")
                .setUrl(SMS_ENDPOINT)
                .setBody(smsRequest.toString());

        JSONObject smsResponse = asyncHttp.sendRequest(requestBody);

        log.info("SMS RESPONSE : {}", smsResponse);

        return smsResponse;

    }
}
