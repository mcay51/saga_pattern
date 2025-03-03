package tr.com.mcay.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

/**
 * Saga Pattern Orchestration uygulaması ana sınıfı
 */
@SpringBootApplication
public class OrchestrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestrationApplication.class, args);
    }

    /**
     * Diğer servislerle iletişim kurmak için RestTemplate bean'i
     * @return RestTemplate nesnesi
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 