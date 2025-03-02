package tr.com.mcay.orchestration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API yanıtları için genel DTO sınıfı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Başarılı yanıt oluşturur
     * @param message Mesaj
     * @param data Veri
     * @return ApiResponse nesnesi
     * @param <T> Veri tipi
     */
    public static <T> CustomApiResponse<T> success(String message, T data) {
        return CustomApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Başarılı yanıt oluşturur (veri olmadan)
     * @param message Mesaj
     * @return ApiResponse nesnesi
     */
    public static CustomApiResponse<Void> success(String message) {
        return CustomApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Hata yanıtı oluşturur
     * @param message Hata mesajı
     * @return ApiResponse nesnesi
     */
    public static CustomApiResponse<Void> error(String message) {
        return CustomApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }
} 