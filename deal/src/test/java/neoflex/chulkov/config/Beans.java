package neoflex.chulkov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class Beans {
    @Bean
    @Primary
    public RestClient getRestClient(
            @Value("${deal.calculator-base-url:http://localhost:54321}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}