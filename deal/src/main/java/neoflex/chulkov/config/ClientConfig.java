package neoflex.chulkov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClient getRestClient(
            RestClient.Builder builder,
            @Value("${deal.calculator-base-url:http://localhost:8081}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
