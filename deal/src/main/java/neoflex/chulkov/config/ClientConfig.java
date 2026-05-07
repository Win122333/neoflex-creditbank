package neoflex.chulkov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClient getRestClient(
            RestClient.Builder builder,
            @Value("${deal.calculator-base-url:http://localhost:8081}") String baseUrl,
            LoadBalancerClient loadBalancerClient
    ) {
        return builder
                .baseUrl(baseUrl)
                .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                .build();
    }
}
