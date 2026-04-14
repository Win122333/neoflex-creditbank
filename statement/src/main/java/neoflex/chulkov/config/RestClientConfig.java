package neoflex.chulkov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient getRestClient(
            RestClient.Builder builder,
            @Value("${statement.deal-base-url:localhost:8082}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
