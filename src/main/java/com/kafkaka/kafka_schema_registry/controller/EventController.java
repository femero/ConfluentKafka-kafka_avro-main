package com.kafkaka.kafka_schema_registry.controller;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import com.kafkaka.kafka_schema_registry.producer.KafkaAvroProducer21;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/api")
public class EventController {

    @Autowired
    private KafkaAvroProducer21 producer;

    @PostMapping("/events")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendMessage(@RequestBody orderRecord order) {
        log.info("🔵 NUEVA PETICIÓN RECIBIDA - POST /api/events");
        log.info("   Order ID: {}", order.getOrderId());
        log.info("   Description: {}", order.getOrderDescription());
        log.info("   Address: {}", order.getOrderAddress());

        // Validación de campos
        List<String> validationErrors = validateOrder(order);
        if (!validationErrors.isEmpty()) {
            log.warn("⚠️  VALIDACIÓN FALLIDA: {} errores encontrados", validationErrors.size());
            validationErrors.forEach(error -> log.warn("   - {}", error));
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                            createValidationErrorResponse(validationErrors, order)
                    )
            );
        }

        return producer.send(order)
                .thenApply(message -> {
                    log.info("✅ RESPUESTA EXITOSA ENVIADA AL CLIENTE");
                    return ResponseEntity.ok(createSuccessResponse(message, order));
                })
                .exceptionally(ex -> {
                    log.error("❌ ERROR EN EL PROCESAMIENTO", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createServerErrorResponse(ex, order));
                });
    }

    private List<String> validateOrder(orderRecord order) {
        List<String> errors = new ArrayList<>();

        if (order.getOrderId() == null || order.getOrderId() <= 0) {
            errors.add("orderId debe ser un número positivo mayor a 0");
        }

        if (order.getOrderDescription() == null || order.getOrderDescription().toString().trim().isEmpty()) {
            errors.add("orderDescription es obligatorio y no puede estar vacío");
        } else if (order.getOrderDescription().toString().length() < 5) {
            errors.add("orderDescription debe tener al menos 5 caracteres");
        }

        if (order.getOrderAddress() == null || order.getOrderAddress().toString().trim().isEmpty()) {
            errors.add("orderAddress es obligatorio y no puede estar vacío");
        } else if (order.getOrderAddress().toString().length() < 5) {
            errors.add("orderAddress debe tener al menos 5 caracteres");
        }

        return errors;
    }

    private Map<String, Object> createSuccessResponse(String message, orderRecord order) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("timestamp", getCurrentTimestamp());
        response.put("message", message);
        response.put("data", Map.of(
                "orderId", order.getOrderId(),
                "orderDescription", order.getOrderDescription().toString(),
                "orderAddress", order.getOrderAddress().toString()
        ));
        return response;
    }

    private Map<String, Object> createValidationErrorResponse(List<String> errors, orderRecord order) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("timestamp", getCurrentTimestamp());
        response.put("error", "Validación fallida");
        response.put("message", "La solicitud contiene errores de validación. Por favor, corrija los siguientes campos:");
        response.put("validationErrors", errors);
        response.put("errorCount", errors.size());

        // Incluir datos recibidos para debugging
        Map<String, Object> receivedData = new LinkedHashMap<>();
        receivedData.put("orderId", order.getOrderId());
        receivedData.put("orderDescription", order.getOrderDescription() != null ? order.getOrderDescription().toString() : null);
        receivedData.put("orderAddress", order.getOrderAddress() != null ? order.getOrderAddress().toString() : null);
        response.put("receivedData", receivedData);

        response.put("help", "Asegúrese de enviar todos los campos requeridos con los valores correctos");
        return response;
    }

    private Map<String, Object> createServerErrorResponse(Throwable ex, orderRecord order) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("timestamp", getCurrentTimestamp());
        response.put("error", "Error interno del servidor");
        response.put("message", "Ocurrió un error al procesar su solicitud");

        // Determinar el tipo de error
        String errorType = "UNKNOWN_ERROR";
        String errorDetail = ex.getMessage();
        String suggestion = "Por favor, intente nuevamente. Si el problema persiste, contacte al administrador.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Kafka") || ex.getMessage().contains("broker")) {
                errorType = "KAFKA_CONNECTION_ERROR";
                errorDetail = "No se pudo conectar con el servidor de mensajería Kafka";
                suggestion = "Verifique que el servicio de Kafka esté en ejecución y accesible.";
            } else if (ex.getMessage().contains("Schema") || ex.getMessage().contains("registry")) {
                errorType = "SCHEMA_REGISTRY_ERROR";
                errorDetail = "Error al validar el esquema del mensaje con el Schema Registry";
                suggestion = "Verifique que el Schema Registry esté en ejecución y el esquema esté registrado correctamente.";
            } else if (ex.getMessage().contains("Serializ")) {
                errorType = "SERIALIZATION_ERROR";
                errorDetail = "Error al serializar el mensaje en formato Avro";
                suggestion = "Verifique que los datos enviados coincidan con el esquema Avro definido.";
            } else if (ex.getMessage().contains("timeout") || ex.getMessage().contains("Timeout")) {
                errorType = "TIMEOUT_ERROR";
                errorDetail = "La operación excedió el tiempo de espera";
                suggestion = "El servidor está tardando demasiado en responder. Intente nuevamente.";
            }
        }

        response.put("errorType", errorType);
        response.put("errorDetail", errorDetail);
        response.put("suggestion", suggestion);
        response.put("orderId", order.getOrderId());

        // Solo incluir stack trace en desarrollo (puedes controlarlo con un profile)
        if (log.isDebugEnabled()) {
            response.put("technicalDetails", ex.toString());
        }

        return response;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        log.error("❌ EXCEPCIÓN NO CONTROLADA", ex);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("timestamp", getCurrentTimestamp());
        response.put("error", "Error inesperado");
        response.put("message", "Ocurrió un error inesperado al procesar su solicitud");
        response.put("errorType", ex.getClass().getSimpleName());
        response.put("errorDetail", ex.getMessage());
        response.put("suggestion", "Por favor, verifique el formato de su solicitud y los datos enviados.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}