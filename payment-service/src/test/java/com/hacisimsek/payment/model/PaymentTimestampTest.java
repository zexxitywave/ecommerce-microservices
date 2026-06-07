package com.hacisimsek.payment.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTimestampTest {

    @Test
    void onCreateSetsCurrentInstantCreatedAt() {
        Payment payment = new Payment();
        Instant beforeCreate = Instant.now();

        payment.onCreate();

        Instant afterCreate = Instant.now();
        assertThat(payment.getCreatedAt()).isBetween(beforeCreate, afterCreate);
    }
}
