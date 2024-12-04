package com.kafkaka.kafka_schema_registry.controller;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import com.kafkaka.kafka_schema_registry.producer.KafkaAvroProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class EventController {

    @Autowired
    private KafkaAvroProducer producer;

    @PostMapping("/events")
    public CompletableFuture<String> sendMessage(@RequestBody orderRecord employee) {
       return producer.send(employee);
    }
}
