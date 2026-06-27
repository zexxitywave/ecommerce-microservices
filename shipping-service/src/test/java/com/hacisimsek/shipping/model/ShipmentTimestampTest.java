package com.hacisimsek.shipping.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentTimestampTest {

    @Test
    void onCreateSetsCurrentInstantTimestamps() {
        Shipment shipment = new Shipment();
        Instant beforeCreate = Instant.now();

        shipment.onCreate();

        Instant afterCreate = Instant.now();
        assertThat(shipment.getCreatedAt()).isBetween(beforeCreate, afterCreate);
        assertThat(shipment.getLastModifiedAt()).isEqualTo(shipment.getCreatedAt());
    }

    @Test
    void onUpdateRefreshesOnlyLastModifiedTimestamp() {
        Shipment shipment = new Shipment();
        Instant createdAt = Instant.EPOCH;
        shipment.setCreatedAt(createdAt);
        shipment.setLastModifiedAt(createdAt);

        shipment.onUpdate();

        assertThat(shipment.getCreatedAt()).isEqualTo(createdAt);
        assertThat(shipment.getLastModifiedAt()).isAfter(createdAt);
    }
}
