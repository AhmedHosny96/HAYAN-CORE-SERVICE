package com.hayaan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration

public class OpenApiConfig {

    @Value("${servers.test-url}")
    private String DEV_URL;

    @Value("${servers.prod-url}")
    private String PROD_URL;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(DEV_URL);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(PROD_URL);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail("aahosny1@gmail.com");
        contact.setName("Ahmed Hosny");
        contact.setUrl("https://www.linkedin.com/in/ahmed-hosny-594761177/");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Flight Booking and Management API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage flights , book flights .").termsOfService("https://www.linkedin.com/in/ahmed-hosny-594761177/")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}
