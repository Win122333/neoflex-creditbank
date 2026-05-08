package neoflex.chulkov.config;

import neoflex.chulkov.client.StatementRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StatementClientConfig {
    @Bean
    public StatementRestClient statementRestClient(
        RestClient.Builder builder,
        @Value("${statement.base-url}") String baseUrl
    ) {
        return new StatementRestClient(builder
            .baseUrl(baseUrl)
            .build()
        );
    }
}
