Detener y eliminar contenedores
docker-compose down

Detener y eliminar contenedores, redes y volúmenes
docker-compose down --volumes

Limpiar imágenes y recursos no utilizados (opcional)
docker system prune -a

Elimine cualquier volumen persistente (si existe)
docker volume prune -f

PASO 1
docker-compose up -d

docker network inspect kafka_avro-main_microservices-network

PASO 2
docker-compose up -d zookeeper
docker logs -f zookeeper

PASO 3
docker-compose up -d kafka
docker logs -f kafka

PASO 4
docker-compose up -d schema-registry
docker logs -f schema-registry

PASO 5
curl -X POST http://localhost:8081/subjects/schema-test-value/versions \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     -d '{"schema":"{\"name\": \"orderRecord\", \"type\": \"record\",  \"doc\": \"Sample schema to help you get started.\", \"fields\": [{\"name\": \"orderId\", \"type\": \"int\", \"doc\": \"The id of the order.\"}, {\"name\": \"orderDescription\", \"type\": \"string\", \"doc\": \"The description of the order.\"}, {\"name\": \"orderAddress\", \"type\": \"string\", \"doc\": \"The address of the order.\"}]}"}'

curl -X GET http://localhost:8081/subjects

PASO 6
docker exec -it kafka kafka-topics \
    --create \
    --topic order-topic \
    --bootstrap-server localhost:9092 \
    --partitions 3 \
    --replication-factor 1

PASO 7
docker-compose up -d kafka_avro-main
docker logs -f kafka_avro-main
docker stop kafka_avro-main
docker rm kafka_avro-main
