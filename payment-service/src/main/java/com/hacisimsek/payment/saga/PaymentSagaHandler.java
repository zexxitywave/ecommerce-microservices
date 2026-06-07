package com.hacisimsek.payment.saga;

import com.hacisimsek.common.event.inventory.InventoryReservedEvent;
import com.hacisimsek.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaHandler {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "inventory-events",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleInventoryEvents(ConsumerRecord<String, Object> record) {

        Object event = record.value();

        log.info("========== PAYMENT EVENT RECEIVED ==========");
        log.info("Event type: {}", event != null ? event.getClass().getName() : "null");
        log.info("Event value: {}", event);

        if (event instanceof InventoryReservedEvent inventoryReservedEvent) {

            log.info("✅ Processing InventoryReservedEvent");
            log.info("Order ID: {}", inventoryReservedEvent.getOrderId());

            paymentService.processPayment(inventoryReservedEvent);

        } else {
            log.warn("❌ Event is NOT InventoryReservedEvent");
        }
    }
}
