package demo.dynassl;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SslController {

    Logger logger = LoggerFactory.getLogger(SslController.class);

    // private static final String TOMCAT_MBEAN_NAME = "Tomcat:type=ThreadPool,name=*";
    private static final String TOMCAT_MBEAN_NAME = "Tomcat:type=ProtocolHandler,port=*";
    private static final String TOMCAT_MBEAN_METHOD_NAME = "reloadSslHostConfigs";

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
    @GetMapping("/jmxreload")
    public String jmxreload() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(TOMCAT_MBEAN_NAME);
            Set<ObjectInstance> allTP = server.queryMBeans(objectName, null);
            logger.info("Tomcat MBeans found: {}", allTP.size());
            allTP.forEach(tp -> {
                logger.info("Invoking operation {} on {}", TOMCAT_MBEAN_METHOD_NAME, tp.getObjectName());
                try {
                    server.invoke(tp.getObjectName(), TOMCAT_MBEAN_METHOD_NAME, new Object[] {}, new String[] {});
                } catch (Exception ex) {
                    logger.warn("Invoking {}", TOMCAT_MBEAN_METHOD_NAME, ex);
                }
                logger.trace("Successfully invoked");
            });
        } catch (Exception ex) {
            logger.warn("JMX Error", ex);
        }
		return "OK";
    }
    
    @Autowired private SslConfigCustomizer customizer;

    /**
      * https://stackoverflow.com/questions/39527478/can-we-avoid-spring-boot-application-restart-to-refresh-the-certificates-associ
    * 
     * @return
     */
    @GetMapping("/tccreload")
    public String tccreload() {
        customizer.reloadSslHostConfig();
        return "OK";
    }
}