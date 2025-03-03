package tr.com.mcay.choreography.service;

import tr.com.mcay.choreography.dto.PaymentRequest;
import tr.com.mcay.choreography.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    void refundPayment(Long orderId);
} 