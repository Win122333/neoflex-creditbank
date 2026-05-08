package neoflex.chulkov.config;

import neoflex.chulkov.client.DealRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DealClientConfig {
    @Bean
    public DealRestClient dealRestClient(
        RestClient.Builder builder,
        @Value("${deal.base-url}") String baseUrl
    ) {
        return new DealRestClient(builder
            .baseUrl(baseUrl)
            .build()
        );
    }
}
