package tr.com.mcay.orchestration.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tr.com.mcay.orchestration.dto.CustomApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Tüm istisnaları yakalayan global exception handler
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ResourceNotFoundException istisnasını yakalar
     * @param ex İstisna
     * @return Hata yanıtı
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<CustomApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Kaynak bulunamadı: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CustomApiResponse.error(ex.getMessage()));
    }

    /**
     * Doğrulama hatalarını yakalar
     * @param ex İstisna
     * @return Hata yanıtı
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CustomApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Doğrulama hatası: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Doğrulama hatası")
                        .data(errors)
                        .build());
    }

    /**
     * Diğer tüm istisnaları yakalar
     * @param ex İstisna
     * @return Hata yanıtı
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CustomApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("Beklenmeyen hata: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomApiResponse.error("Beklenmeyen bir hata oluştu: " + ex.getMessage()));
    }
} 