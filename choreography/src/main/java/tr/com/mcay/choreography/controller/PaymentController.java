package tr.com.mcay.choreography.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.mcay.choreography.dto.PaymentRequest;
import tr.com.mcay.choreography.dto.PaymentResponse;
import tr.com.mcay.choreography.service.PaymentService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Void> refundPayment(@PathVariable Long orderId) {
        paymentService.refundPayment(orderId);
        return ResponseEntity.ok().build();
    }
} 