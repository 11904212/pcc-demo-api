package at.ac.tuwien.ba.demo.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI config() {
        final Info info = new Info();
        info.title("pcc-demo-API");
        info.description("OpenAPI documentation the pcc-demo-API endpoints");
        return new OpenAPI().info(info);
    }

}
