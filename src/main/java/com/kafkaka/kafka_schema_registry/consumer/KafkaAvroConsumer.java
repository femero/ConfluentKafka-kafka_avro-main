package com.kafkaka.kafka_schema_registry.consumer;

import com.kafkaka.kafka_schema_registry.dto.orderRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaAvroConsumer {

    @KafkaListener(topics = "${topic.name}",
            topicPartitions ={ @TopicPartition(
                    topic ="order-topic",
                    partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0")
            )})
    public void read(ConsumerRecord<String, orderRecord> consumerRecord) {
        String key = consumerRecord.key();
        orderRecord employee = consumerRecord.value();
        log.info("Consumer kafka read0 - Avro message received for key: {}, value: {}, Key: {}, Value: {}, Partition: {}, Offset: {}",
                key,
                employee.toString(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset());

    }

    @KafkaListener(topics = "${topic.name}",
            topicPartitions ={ @TopicPartition(
                    topic ="order-topic",
                    partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "0")
            )})
    public void read2(ConsumerRecord<String, orderRecord> consumerRecord) {
        String key = consumerRecord.key();
        orderRecord employee = consumerRecord.value();
        log.info("Consumer kafka read1 - Avro message received for key: {}, value: {}, Key: {}, Value: {}, Partition: {}, Offset: {}",
                key,
                employee.toString(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset());

    }

    @KafkaListener(topics = "${topic.name}",
            topicPartitions ={ @TopicPartition(
                    topic ="order-topic",
                    partitionOffsets = @PartitionOffset(partition = "2", initialOffset = "0")
            )})
    public void read3(ConsumerRecord<String, orderRecord> consumerRecord) {
        String key = consumerRecord.key();
        orderRecord employee = consumerRecord.value();
        log.info("Consumer kafka read2 - Avro message received for key: {}, value: {}, Key: {}, Value: {}, Partition: {}, Offset: {}",
                key,
                employee.toString(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset());

    }

}
