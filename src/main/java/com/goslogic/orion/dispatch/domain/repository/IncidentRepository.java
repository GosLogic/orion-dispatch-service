package com.goslogic.orion.dispatch.domain.repository;

import com.goslogic.orion.dispatch.domain.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByExternalId(String externalId);
}
