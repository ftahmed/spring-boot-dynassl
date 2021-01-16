package demo.dynassl;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    Logger logger = LoggerFactory.getLogger(GreetingController.class);

    private static final String TEMPLATE = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name));
    }

    /**
     * https://stackoverflow.com/questions/39527478/can-we-avoid-spring-boot-application-restart-to-refresh-the-certificates-associ
     * https://stackoverflow.com/questions/57997242/embedded-tomcat-update-delete-certificates-without-restarting
     * https://people.apache.org/~schultz/ApacheCon%20NA%202018/Let's%20Encrypt%20Apache%20Tomcat.pdf
     * https://github.com/apache/tomcat/blob/9.0.x/java/org/apache/tomcat/util/net/AbstractEndpoint.java#L244
     * http://tomcat.10.x6.nabble.com/JMX-reloadSslHostConfigs-fails-with-javax-management-RuntimeOperationsException-td5092951.html
     * http://tomcat.10.x6.nabble.com/Dynamic-reloading-of-SSL-certificates-td5059619.html
     * https://github.com/rmannibucau/letsencrypt-manager/blob/master/src/main/java/com/github/rmannibucau/letsencrypt/manager/LetsEncryptManager.java
     * 
     * @return
     */
    @GetMapping("/lecr")
    public String lecr() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            //ObjectName objectName = new ObjectName("Tomcat:type=ThreadPool,name=*");
            ObjectName objectName = new ObjectName("Tomcat:type=ProtocolHandler,port=*");
            Set<ObjectInstance> allTP = server.queryMBeans(objectName, null);
            logger.info("MBeans found: {}", allTP.size());
            allTP.forEach(tp -> {
                logger.info("Invoking operation SSL reload on {}", tp.getObjectName());
                try {
                    server.invoke(tp.getObjectName(), "reloadSslHostConfigs", new Object[] {}, new String[] {});
                } catch (Exception ex) {
                    logger.error("Invoking SSL reload", ex);
                }
                logger.trace("Successfully invoked");
            });
        } catch (Exception ex) {
            logger.error("Error", ex);
        }
		return "OK";
	}
}