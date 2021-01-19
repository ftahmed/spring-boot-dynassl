# Dynamic certificate update

Demo Spring Boot 2.x application dynamically updating certificate using JMX:
* JMXTerm
* Jolokia
* Programatically

## JMXTerm
```
$ echo "get -s -b Tomcat:type=ProtocolHandler,port=8443 secure" | java -jar ./target/jmxterm-1.1.0-SNAPSHOT-uber.jar -l localhost:9010 -n -v silent
true

$ echo "run -b Tomcat:type=ProtocolHandler,port=8443 reloadSslHostConfigs" | java -jar ./target/jmxterm-1.1.0-SNAPSHOT-uber.jar -l localhost:9010 -n 
Welcome to JMX terminal. Type "help" for available commands.
#calling operation reloadSslHostConfigs of mbean Tomcat:type=ProtocolHandler,port=8443 with params []
#operation returns: 
null
```

## Jolokia

Enabled Spring Boot Actuator with Jolokia. Invoke Jolokia URL.

```
$ curl -sk "https://localhost:8443/actuator/jolokia/search/Tomcat:type=ProtocolHandler,port=*" | jq .
{
  "request": {
    "mbean": "Tomcat:port=*,type=ProtocolHandler",
    "type": "search"
  },
  "value": [
    "Tomcat:port=8443,type=ProtocolHandler",
    "Tomcat:port=8080,type=ProtocolHandler"
  ],
  "timestamp": 1610998589,
  "status": 200
}

$ curl -sk "https://localhost:8443/actuator/jolokia/exec/Tomcat:type=ProtocolHandler,port=8443/reloadSslHostConfigs" | jq .
{
  "request": {
    "mbean": "Tomcat:port=8443,type=ProtocolHandler",
    "type": "exec",
    "operation": "reloadSslHostConfigs"
  },
  "value": null,
  "timestamp": 1610998703,
  "status": 200
}
```

## Programatically

### JMX 
Exposed an endpoint `/jmxreload` in `SslController`.
```
$ curl -sk https://localhost:8443/jmxreload
OK
```

### Spring Boot
Implemented `TomcatConnectorCustomizer`. Exposed an endpoint `/tccreload` in `SslController`.
```
$ curl -sk https://localhost:8443/tccreload
OK
```

# Links

* [Basic Introduction to JMX](https://www.baeldung.com/java-management-extensions)
* [Spring Boot JMX](https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/html/production-ready-jmx.html)
* [Spring Boot Remote JMX](https://www.giladpeleg.com/blog/spring-boot-remote-jmx/)
* [How to access Spring-boot JMX remotely](https://stackoverflow.com/questions/29412072/how-to-access-spring-boot-jmx-remotely)
* [How to set JVM settings in a Spring Boot application](http://www.masterspringboot.com/getting-started/spring-boot-introduction/how-to-set-jvm-settings-in-a-spring-boot-application)
* [JMXTerm scripting](https://docs.cyclopsgroup.org/jmxterm/scripting)
* [Jolokia protocol](https://jolokia.org/reference/html/protocol.html#exec)
* [Jmx4Perl](https://metacpan.org/release/jmx4perl)
* [Can we avoid spring boot application restart, to refresh the certificates associated with its embedded tomcat container?](https://stackoverflow.com/questions/39527478/can-we-avoid-spring-boot-application-restart-to-refresh-the-certificates-associ)
