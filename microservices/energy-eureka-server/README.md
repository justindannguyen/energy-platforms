# Eureka server

## Environment Configurations
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
  eureka-server:
    image: justindannguyen/energy-sink
    restart: always
    ports:
      - "8761:8761"
```

Then access by using ```http://localhost:8761```