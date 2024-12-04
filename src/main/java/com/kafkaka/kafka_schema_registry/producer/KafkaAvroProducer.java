package com.kafkaka.kafka_schema_registry.producer;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaAvroProducer {

    @Value("${topic.name}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, orderRecord> template;

    public CompletableFuture<String> send(orderRecord orderRecord) {
        CompletableFuture<SendResult<String, orderRecord>> future =
                template.send(topicName, UUID.randomUUID().toString(), orderRecord);


        return future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent message=[" + orderRecord +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send message=[" +
                        orderRecord + "] due to : " + ex.getMessage());
            }
        }).thenApply(result -> {
            if (result != null) {
                return "Message sent successfully to Kafka with offset: "
                        + result.getRecordMetadata().offset()
                        + " to partition: "
                        + result.getRecordMetadata().partition();
            } else {
                return "Failed to send message to Kafka";
            }
        });
    }
}
