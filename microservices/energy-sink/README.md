# Sink energy into Mongodb

## Environment Configurations
### Eureka configuration
If not set then use default value ```http://localhost:8761/eureka/``` 
```
export EUREKA_SERVER_URI=?
```

### Input topic, usually from Kafka
If not set then use default value ```energysolution_parsedreading```
```
export ENERGY_INPUT_TOPIC=?
```

### Kafka Consumer group
If not set then use default value ```energy-sink``` 
```
export ENEGY_CONSUMER_GROUP=?
```

### Kafka Server
If not set then use default value ```localhost:9092``` 
```
export BROKERS=?
```

### MongoDB configuration
If not set then use default value ```mongodb://localhost:27017/energy``` 
```
export MONGODB_SERVER_URI=?
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
  energy-sink:
    image: justindannguyen/energy-sink
    restart: always
```
