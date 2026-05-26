package neoflex.chulkov.config;

import neoflex.chulkov.client.deal.DealApi;
import neoflex.chulkov.client.deal.DocumentApi;
import neoflex.chulkov.client.statement.StatementApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
public class ClientConfig {
    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public DealApi dealApi(RestClient.Builder builder, @Value("${deal.base-url:http://deal}") String url) {
        return createClient(builder, url, DealApi.class);
    }

    @Bean
    public DocumentApi documentApi(RestClient.Builder builder, @Value("${deal.base-url:http://deal}") String url) {
        return createClient(builder, url, DocumentApi.class);
    }

    @Bean
    public StatementApi statementApi(RestClient.Builder builder, @Value("${statement.base-url:http://calculator}") String url) {
        return createClient(builder, url, StatementApi.class);
    }

    private <T> T createClient(RestClient.Builder builder, String url, Class<T> clientClass) {
        RestClient restClient = builder.baseUrl(url).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(clientClass);
    }
}