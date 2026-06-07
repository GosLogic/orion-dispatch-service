package com.goslogic.orion.dispatch.presentation.dto;

/** Respuesta minimalista de los endpoints de sincronización (start/end/arrived/delivery). */
public record StatusResponse(String id, String status) {}
