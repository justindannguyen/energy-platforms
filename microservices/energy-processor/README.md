# Transform Raw Energy into Stream

## Environment Configurations
### Eureka configuration
If not set then use default value ```http://localhost:8761/eureka/``` 
```
export EUREKA_SERVER_URI=?
```

### Kafka input topic
If not set then use default value ```energysolution_rawreading```
```
export ENERGY_INPUT_TOPIC=?
```

### Kafka Consumer group, raw
If not set then use default value ```energy-processor``` 
```
export ENEGY_CONSUMER_GROUP=?
```

### Kafka Consumer group, sink
If not set then use default value ```energy-sink``` 
```
export ENEGY_SINK_GROUP=?
```

### Kafka Server
If not set then use default value ```localhost:9092``` 
```
export BROKERS=?
```

### Kafka output topic
If not set then use default value ```energysolution_parsedreading``` 
```
export ENERGY_OUTPUT_TOPIC=?
```

## Build
Build at local
```
mvn clean install
```

Build & publish as docker image
```
mvn clean install -P docker
```

## Use the image
```
version: '2'
services:
  energy-processor:
    image: justindannguyen/energy-processor
    restart: always
```

