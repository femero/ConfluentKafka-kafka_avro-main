package com.kafkaka.kafka_schema_registry.producer;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KafkaAvroProducer {

    @Value("${topic.name:order-topic}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, orderRecord> template;

    public CompletableFuture<String> send(orderRecord order) {
        String messageKey = UUID.randomUUID().toString();

        log.info("📤 ENVIANDO MENSAJE A KAFKA");
        log.info("   Topic: {}", topicName);
        log.info("   Order ID: {}", order.getOrderId());
        log.info("   Description: {}", order.getOrderDescription());
        log.info("   Address: {}", order.getOrderAddress());

        CompletableFuture<SendResult<String, orderRecord>> future =
                template.send(topicName, 1, messageKey, order);

        return future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ MENSAJE ENVIADO EXITOSAMENTE");
                log.info("   Partition: {}", result.getRecordMetadata().partition());
                log.info("   Offset: {}", result.getRecordMetadata().offset());
                log.info("   Timestamp: {}", result.getRecordMetadata().timestamp());
            } else {
                log.error("❌ ERROR AL ENVIAR MENSAJE");
                log.error("   Order ID: {}", order.getOrderId());
                log.error("   Error: {}", ex.getMessage(), ex);
            }
        }).thenApply(result -> {
            if (result != null) {
                return String.format(
                        "✅ Orden #%d enviada exitosamente a Kafka | Partition: %d | Offset: %d",
                        order.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                return "❌ Error: No se pudo enviar el mensaje a Kafka";
            }
        });
    }
}