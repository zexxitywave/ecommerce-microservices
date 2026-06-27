package com.hacisimsek.payment.gateway;

import com.hacisimsek.payment.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock gateway — always succeeds.
 * Used by the saga auto-processing flow and local dev.
 * Active on all profiles (can be restricted to "!prod" if needed).
 */
@Component
@Slf4j
public class MockGatewayAdapter implements PaymentGatewayAdapter {

    @Override
    public Payment.PaymentGateway getGateway() {
        return Payment.PaymentGateway.MOCK;
    }

    @Override
    public GatewayOrderResult createOrder(UUID internalPaymentId, BigDecimal amount, String currency) {
        log.debug("[Mock] createOrder for payment={}", internalPaymentId);
        return new GatewayOrderResult("mock_order_" + internalPaymentId, "mock_key");
    }

    @Override
    public boolean verifyAndCapture(String gatewayPaymentId, String gatewayOrderId, String gatewaySignature) {
        log.debug("[Mock] verifyAndCapture — always returns true");
        return true;
    }

    @Override
    public String refund(String gatewayPaymentId, BigDecimal amount) {
        log.debug("[Mock] refund for payment={}, amount={}", gatewayPaymentId, amount);
        return "mock_refund_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
