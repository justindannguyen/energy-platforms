# Parent project for device software

## Roadmap
* [x] FOTA parameters & FOTA application via MQTT
* [x] Energy reading and push to kafka server
* [x] Device status and push to kafka server
* [x] Auto reconnect both modbus + mqtt
* [ ] Separate modbus & kafka connection
* [ ] Checksum when download new firmware file.

## Debug Options
### Using jstatd
```
jstatd -J-Djava.security.policy=.jstatd.all.policy -J-Djava.rmi.server.hostname=10.82.83.117 -J-Djava.rmi.server.logCalltrue
```
### JMX
```
-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9000 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=192.168.1.104
```

## Build
Build at local
```
mvn clean install
```