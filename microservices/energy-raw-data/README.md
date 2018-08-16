# HTTP API - Meter Raw reading 

## Environment Configurations
### Eureka configuration
If not set then use default value ```http://localhost:8761/eureka/``` 
```
export EUREKA_SERVER_URI=?
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
  energy-raw-data:
    image: justindannguyen/energy-raw-data
    restart: always
    ports:
      - "8080:8080"
```
Then access ```http://localhost:8080```