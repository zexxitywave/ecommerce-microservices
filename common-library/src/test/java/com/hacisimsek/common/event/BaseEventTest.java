package com.hacisimsek.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEventTest {

    @Test
    void constructorSetsCurrentInstantTimestamps() {
        UUID correlationId = UUID.randomUUID();
        Instant beforeCreate = Instant.now();

        TestEvent event = new TestEvent(correlationId);

        Instant afterCreate = Instant.now();
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
        assertThat(event.getTimestamp()).isBetween(beforeCreate, afterCreate);
        assertThat(event.getCreatedAt()).isEqualTo(event.getTimestamp());
    }

    private static class TestEvent extends BaseEvent {
        TestEvent(UUID correlationId) {
            super(correlationId);
        }
    }
}
