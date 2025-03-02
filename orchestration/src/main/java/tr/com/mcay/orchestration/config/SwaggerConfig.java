package tr.com.mcay.orchestration.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI yapılandırma sınıfı
 */
@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    /**
     * OpenAPI yapılandırması
     * @return OpenAPI nesnesi
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Saga Orchestration API")
                        .version("1.0")
                        .description("Saga Pattern Orchestration Service API Documentation")
                        .contact(new Contact()
                                .name("Mustafa ÇAY")
                                .email("mcay51@gmail.com")
                                .url("https://www.mustafacay.com.tr"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
