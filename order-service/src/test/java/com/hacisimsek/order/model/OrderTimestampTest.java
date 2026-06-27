package com.hacisimsek.order.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTimestampTest {

    @Test
    void onCreateSetsCurrentInstantTimestamps() {
        Order order = new Order();
        Instant beforeCreate = Instant.now();

        order.onCreate();

        Instant afterCreate = Instant.now();
        assertThat(order.getCreatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(order.getLastModifiedAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    void onUpdateRefreshesOnlyLastModifiedTimestamp() {
        Order order = new Order();
        Instant createdAt = Instant.EPOCH;
        order.setCreatedAt(createdAt);
        order.setLastModifiedAt(createdAt);

        order.onUpdate();

        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getLastModifiedAt()).isAfter(createdAt);
    }
}
