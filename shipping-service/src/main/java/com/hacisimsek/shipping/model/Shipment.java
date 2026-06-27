package com.hacisimsek.shipping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;
    private UUID customerId;
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private String trackingNumber;
    private String carrierName;
    private Instant shippedDate;
    private Instant estimatedDeliveryDate;

    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;

    @Column(updatable = false)
    private Instant createdAt;
    private String createdBy;
    private Instant lastModifiedAt;
    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.lastModifiedAt = now;
        this.createdBy = "simsekhaci";
        this.lastModifiedBy = "simsekhaci";
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedAt = Instant.now();
        this.lastModifiedBy = "simsekhaci";
    }

    public enum ShipmentStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
}
