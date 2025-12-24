package com.kafkaka.kafka_schema_registry.producer;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class KafkaAvroProducer21 {

    @Value("${topic.name:order-topic}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, orderRecord> template;

    // ExecutorService con Virtual Threads de Java 21
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Value("${kafka.partition.default:#{null}}")
    private Integer defaultPartition;

    public CompletableFuture<String> send(orderRecord order) {
        // Ejecutar en un Virtual Thread para mejor concurrencia
        return CompletableFuture.supplyAsync(() -> {
            String messageKey = UUID.randomUUID().toString();

            log.info(" EJECUTANDO EN VIRTUAL THREAD: {}", Thread.currentThread());
            log.info(" ENVIANDO MENSAJE A KAFKA");
            log.info("   Topic: {}", topicName);
            log.info("   Order ID: {}", order.getOrderId());
            log.info("   Description: {}", order.getOrderDescription());
            log.info("   Address: {}", order.getOrderAddress());

            try {
                CompletableFuture<SendResult<String, orderRecord>> future;
                // Enviar mensaje a Kafka
                if (defaultPartition != null) {
                    future = template.send(topicName, defaultPartition, messageKey, order);
                } else {
                    future = template.send(topicName, messageKey, order);
                }
                // Enviar mensaje a Kafka
                /*CompletableFuture<SendResult<String, orderRecord>> future =
                        template.send(topicName, 1, messageKey, order);
*/
                // Esperar el resultado (blocking en Virtual Thread - muy eficiente)
                SendResult<String, orderRecord> result = future.get();

                log.info(" MENSAJE ENVIADO EXITOSAMENTE");
                log.info("   Partition: {}", result.getRecordMetadata().partition());
                log.info("   Offset: {}", result.getRecordMetadata().offset());
                log.info("   Timestamp: {}", result.getRecordMetadata().timestamp());

                return String.format(
                        " Orden #%d enviada exitosamente a Kafka | Partition: %d | Offset: %d",
                        order.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } catch (Exception ex) {
                log.error(" ERROR AL ENVIAR MENSAJE");
                log.error("   Order ID: {}", order.getOrderId());
                log.error("   Error: {}", ex.getMessage(), ex);
                throw new RuntimeException("Error al enviar mensaje a Kafka", ex);
            }
        }, virtualThreadExecutor);
    }

    @PreDestroy
    public void shutdown() {
        log.info(" Cerrando Virtual Thread Executor...");
        virtualThreadExecutor.shutdown();
    }
}