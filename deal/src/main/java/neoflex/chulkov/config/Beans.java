package neoflex.chulkov.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class Beans {
    @Bean
    public RestClient getRestClient(
            @Value("${deal.calculator-base-url:http://localhost:8081}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Микросервиса Deal")
                        .version("1.0.0")
                        .description("Микросервис для работы с заявками на кредит и расчетами."));
    }
}
