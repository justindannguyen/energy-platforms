# Tracing server

## Environment Configurations
### Web port
If not set then use default value ```9411```
```
export PORT=?
```

### Eureka configuration
If not set then use default value ```http://localhost:8761/eureka/``` 
```
export EUREKA_SERVER_URI=?
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
  zipkin-server:
    image: justindannguyen/zinkin-server
    restart: always
    ports:
      - "9411:9411"
```
