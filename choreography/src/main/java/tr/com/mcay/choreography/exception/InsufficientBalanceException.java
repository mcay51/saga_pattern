package tr.com.mcay.choreography.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(Long customerId, BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance for customer %d. Required: %s, Available: %s", 
                customerId, required.toString(), available.toString()));
    }
} 