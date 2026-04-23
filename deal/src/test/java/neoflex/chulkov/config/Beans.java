package neoflex.chulkov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class Beans {
    @Bean
    @Primary
    public RestClient testRestClient(
            @Value("${statement.deal-base-url:http://localhost:54321}") String baseUrl
    ) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(restTemplate.getRequestFactory())
                .build();
    }
}