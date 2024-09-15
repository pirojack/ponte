package com.ponte.picture_transformation_encypt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {
    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Development");

        Contact myContact = new Contact();
        myContact.setName("Andras Bardi");
        myContact.setEmail("bardi.andras91@gmail.com");

        Info information = new Info()
                .title("Picture transformation with encyption App")
                .version("1.0")
                .description("This API exposes endpoints to upload and download encrypted pictures into postgreSQL database")
                .contact(myContact);

        return new OpenAPI().info(information).servers(List.of(server));
    }
}