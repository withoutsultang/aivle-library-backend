package com.aivle8.backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;
@Configuration
public class ImageApiConfig {
    @Bean
    public RestClient imageApiClient(RestClient.Builder builder) {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(120));
        return builder.baseUrl("https://api.openai.com/v1").requestFactory(factory).build();
    }
}
