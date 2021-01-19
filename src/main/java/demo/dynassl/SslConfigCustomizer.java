package demo.dynassl;

import java.util.Collection;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

public class SslConfigCustomizer implements TomcatConnectorCustomizer {

    public static final String DEFAULT_SSL_HOSTNAME_CONFIG_NAME = "_default_";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ServletWebServerFactory servletWebServerFactory;
    private Http11NioProtocol protocol;

    public SslConfigCustomizer(ServletWebServerFactory servletWebServerFactory) {

        this.servletWebServerFactory = servletWebServerFactory;
    }

    @Override
    public void customize(Connector connector) {

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        if (connector.getSecure()) {
            // --- REMEMBER PROTOCOL WHICH WE NEED LATER IN ORDER TO RELOAD SSL CONFIG
            this.protocol = protocol;
        }
    }

    protected Http11NioProtocol getProtocol() {
        return protocol;
    }

    public void reloadSslHostConfig() {

        TomcatServletWebServerFactory tomcatFactoty = (TomcatServletWebServerFactory) servletWebServerFactory;
        Collection<TomcatConnectorCustomizer> customizers = tomcatFactoty.getTomcatConnectorCustomizers();
        for (TomcatConnectorCustomizer tomcatConnectorCustomizer : customizers) {

            if (tomcatConnectorCustomizer instanceof SslConfigCustomizer) {
                SslConfigCustomizer customizer = (SslConfigCustomizer) tomcatConnectorCustomizer;
                Http11NioProtocol protocol = customizer.getProtocol();
                try {
                    protocol.reloadSslHostConfig(DEFAULT_SSL_HOSTNAME_CONFIG_NAME);
                    logger.info("Reloaded SSL host configuration");
                } catch (IllegalArgumentException e) {
                    logger.warn("Cannot reload SSL host configuration", e);
                }
            }
        }

    }
}
