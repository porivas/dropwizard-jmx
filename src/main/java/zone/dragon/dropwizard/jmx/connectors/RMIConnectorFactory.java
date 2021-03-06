package zone.dragon.dropwizard.jmx.connectors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Maps;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

/**
 * @author Darth Android
 * @date 2/8/2017
 */
@Slf4j
@Data
@JsonTypeName("rmi")
public class RMIConnectorFactory implements JmxConnectorFactory {
    private static final String JMX_SERVICE_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://:%d/jmxrmi";
    @Valid
    @Min(1)
    @Max(65535)
    private              int    port                   = 9000;

    @Override
    public void applyConnector(Environment environment, JMXAuthenticator authenticator) throws Exception {
        LocateRegistry.createRegistry(getPort());
        MBeanServer         mBeanServer = ManagementFactory.getPlatformMBeanServer();
        JMXServiceURL       serviceURL  = new JMXServiceURL(String.format(JMX_SERVICE_URL_FORMAT, getPort()));
        Map<String, Object> config      = Maps.newHashMap();
        config.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        JMXConnectorServer jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, config, mBeanServer);
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                jmxServer.start();
            }

            @Override
            public void stop() throws Exception {
                jmxServer.stop();
            }
        });
    }
}
