package com.goslogic.orion.dispatch.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 100)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private TripStop tripStop;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "proof_type", length = 20)
    private ProofType proofType;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "signature_url", columnDefinition = "TEXT")
    private String signatureUrl;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "delivered_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Delivery(String externalId, TripStop tripStop, String customerName,
                    String description, ProofType proofType, String photoUrl,
                    String signatureUrl, String notes, OffsetDateTime deliveredAt,
                    DeliveryStatus status) {
        this.externalId = externalId;
        this.tripStop = tripStop;
        this.customerName = customerName;
        this.description = description;
        this.proofType = proofType;
        this.photoUrl = photoUrl;
        this.signatureUrl = signatureUrl;
        this.notes = notes;
        this.deliveredAt = deliveredAt;
        this.status = status;
    }
}
