package com.goslogic.orion.dispatch.application;

import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import com.goslogic.orion.dispatch.domain.model.*;
import com.goslogic.orion.dispatch.domain.repository.DeliveryRepository;
import com.goslogic.orion.dispatch.domain.repository.TripStopRepository;
import com.goslogic.orion.dispatch.infrastructure.messaging.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DeliveryApplicationService {

    private final DeliveryRepository deliveryRepository;
    private final TripStopRepository tripStopRepository;
    private final DomainEventPublisher eventPublisher;

    public DeliveryApplicationService(DeliveryRepository deliveryRepository,
                                      TripStopRepository tripStopRepository,
                                      DomainEventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.tripStopRepository = tripStopRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registra una entrega (POST /dispatch/deliveries).
     * Idempotente: si ya existe por external_id devuelve la entrega existente sin error (ON CONFLICT DO NOTHING).
     */
    public Delivery registerDelivery(CreateDeliveryCommand cmd) {
        // Idempotencia por external_id
        Optional<Delivery> existing = deliveryRepository.findByExternalId(cmd.externalId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Resolver trip_stop_id (string) → entidad
        TripStop stop = tripStopRepository
                .findByExternalIdAndRouteSheet_TenantExternalId(cmd.tripStopExternalId(), cmd.tenantExternalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TripStop no encontrada: " + cmd.tripStopExternalId()));

        // ACL: is_completed → status enum
        DeliveryStatus status = cmd.isCompleted() ? DeliveryStatus.DELIVERED : DeliveryStatus.PENDING;

        // ACL: proof_type string → enum (uppercase)
        ProofType proofType = null;
        if (cmd.proofType() != null) {
            proofType = ProofType.valueOf(cmd.proofType().toUpperCase());
        }

        Delivery delivery = new Delivery(
                cmd.externalId(),
                stop,
                cmd.customerName(),
                cmd.packageDescription(),
                proofType,
                cmd.photoPath(),
                cmd.signaturePath(),
                cmd.notes(),
                cmd.deliveredAt() != null ? OffsetDateTime.parse(cmd.deliveredAt()) : null,
                status
        );
        deliveryRepository.save(delivery);

        // Side-effect: si todas las entregas del stop están DELIVERED → marcar stop como COMPLETED
        if (status == DeliveryStatus.DELIVERED) {
            long pendingCount = deliveryRepository.countByTripStop_IdAndStatusNot(stop.getId(), DeliveryStatus.DELIVERED);
            if (pendingCount == 0) {
                stop.complete();
                tripStopRepository.save(stop);
            }
        }

        eventPublisher.publish("orion.dispatch.delivery-completed", Map.of(
                "delivery_id", delivery.getExternalId(),
                "trip_stop_id", stop.getExternalId(),
                "status", status.name()
        ));

        return delivery;
    }

    /** Command object (ACL interna — mapeo desde el DTO del móvil). */
    public record CreateDeliveryCommand(
            String externalId,
            String tripStopExternalId,
            String tenantExternalId,
            String customerName,
            String packageDescription,
            String proofType,
            String photoPath,
            String signaturePath,
            String notes,
            String deliveredAt,
            boolean isCompleted
    ) {}
}
