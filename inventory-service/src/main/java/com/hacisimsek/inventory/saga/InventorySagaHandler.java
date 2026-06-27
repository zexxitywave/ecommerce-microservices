package com.hacisimsek.inventory.saga;

import com.hacisimsek.common.event.order.OrderCreatedEvent;
import com.hacisimsek.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySagaHandler {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-events",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvents(ConsumerRecord<String, Object> record) {

        Object event = record.value();

        log.info("========== EVENT RECEIVED ==========");
        log.info("Event type: {}", event != null ? event.getClass().getName() : "null");
        log.info("Event value: {}", event);

        try {
            if (event instanceof OrderCreatedEvent orderCreatedEvent) {

                log.info("✅ Processing OrderCreatedEvent");
                log.info("Order ID: {}", orderCreatedEvent.getOrderId());

                inventoryService.reserveInventory(orderCreatedEvent);

            } else {

                log.warn("❌ Event is NOT OrderCreatedEvent. Actual type: {}",
                        event != null ? event.getClass().getName() : "null");

            }
        } catch (Exception e) {
            log.error("❌ EXCEPTION in handleOrderEvents", e);
        }
    }
}