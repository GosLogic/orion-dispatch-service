package com.goslogic.orion.dispatch.presentation;

import com.goslogic.orion.dispatch.application.exception.ConflictException;
import com.goslogic.orion.dispatch.application.exception.ForbiddenException;
import com.goslogic.orion.dispatch.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_retorna_404() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleNotFound(new ResourceNotFoundException("Recurso no encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleForbidden_retorna_403() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleForbidden(new ForbiddenException("Acceso denegado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
        assertThat(response.getBody().message()).isEqualTo("Acceso denegado");
    }

    @Test
    void handleConflict_retorna_409() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleConflict(new ConflictException("Conflicto de estado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Conflicto de estado");
    }

    @Test
    void handleIllegalState_retorna_409() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleIllegalState(new IllegalStateException("Estado inválido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo("Estado inválido");
    }

    @Test
    void handleValidation_retorna_400_con_errores_por_campo() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "status", "must not be blank"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorBody> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isInstanceOf(java.util.Map.class);
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) response.getBody().message();
        assertThat(errors).containsEntry("status", "must not be blank");
    }

    @Test
    void handleGeneric_retorna_500() {
        ResponseEntity<GlobalExceptionHandler.ErrorBody> response =
                handler.handleGeneric(new RuntimeException("Error inesperado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("Error inesperado");
    }
}
