package tr.com.mcay.choreography.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.com.mcay.choreography.dto.PaymentRequest;
import tr.com.mcay.choreography.dto.PaymentResponse;
import tr.com.mcay.choreography.entity.Customer;
import tr.com.mcay.choreography.entity.Payment;
import tr.com.mcay.choreography.entity.PaymentStatus;
import tr.com.mcay.choreography.exception.InsufficientBalanceException;
import tr.com.mcay.choreography.exception.ResourceNotFoundException;
import tr.com.mcay.choreography.repository.CustomerRepository;
import tr.com.mcay.choreography.repository.PaymentRepository;
import tr.com.mcay.choreography.service.PaymentService;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Müşteri bakiyesini kontrol et
        if (customer.getBalance().compareTo(request.getAmount()) < 0) {
            // Yetersiz bakiye, ödeme başarısız
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .id(savedPayment.getId())
                    .orderId(savedPayment.getOrderId())
                    .customerId(savedPayment.getCustomerId())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus())
                    .message(String.format("Yetersiz bakiye. Gereken: %s, Mevcut: %s", 
                            request.getAmount(), customer.getBalance()))
                    .build();
        }

        // Ödeme işlemini gerçekleştir
        customer.setBalance(customer.getBalance().subtract(request.getAmount()));
        customerRepository.save(customer);

        // Ödeme kaydını oluştur
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .id(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .customerId(savedPayment.getCustomerId())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus())
                .message("Ödeme başarıyla tamamlandı")
                .build();
    }

    @Override
    @Transactional
    public void refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            // Müşteri bakiyesine iade yap
            Customer customer = customerRepository.findById(payment.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", payment.getCustomerId()));

            customer.setBalance(customer.getBalance().add(payment.getAmount()));
            customerRepository.save(customer);

            // Ödeme durumunu güncelle
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }
    }
} 