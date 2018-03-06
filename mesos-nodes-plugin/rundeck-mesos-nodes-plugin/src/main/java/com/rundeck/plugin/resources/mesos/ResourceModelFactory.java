package com.rundeck.plugin.resources.mesos;

import com.codahale.metrics.MetricRegistry;
import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.rundeck.plugin.resources.mesos.client.DefaultHTTP;
import com.rundeck.plugin.resources.mesos.client.MesosAPI;
import com.rundeck.plugin.resources.mesos.client.MesosNode;

import java.util.Properties;

import static com.dtolabs.rundeck.core.execution.impl.jsch.JschNodeExecutor.FWK_PROP_PREFIX;

@Plugin(name = ResourceModelFactory.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.ResourceModelSource)
public class ResourceModelFactory implements ResourceModelSourceFactory, Describable {
    public static final String SERVICE_PROVIDER_NAME = "com.rundeck.plugin.resources.mesos.ResourceModelFactory";

    private static MetricRegistry METRICS = new MetricRegistry();

    static String PROPERTY_MESOS_HOST = "PROPERTY_MESOS_HOST";
    static String PROPERTY_MESOS_PORT = "PROPERTY_MESOS_PORT";
    static String PROPERTY_MESOS_USERNAME = "PROPERTY_MESOS_USERNAME";
    static String PROPERTY_NODE_QUERY = "PROPERTY_NODE_QUERY";

    public static Description DESC = null;

    private Framework framework;
    private final MetricRegistry metrics;

    public ResourceModelFactory(final Framework framework) {
        this.metrics = METRICS;
        this.framework = framework;
    }

    static {
        DESC = DescriptionBuilder.builder()
                .name(SERVICE_PROVIDER_NAME)
                .title("Mesos Nodes Plugin")
                .description("Produces Nodes from Mesos Service")
                .property(PropertyUtil.string(PROPERTY_MESOS_HOST, "Mesos Host", "Mesos hostname", true, null))
                .property(PropertyUtil.integer(PROPERTY_MESOS_PORT, "Mesos Port", "Mesos port (default 8080)", true, "8080"))
                .property(PropertyUtil.string(PROPERTY_MESOS_USERNAME, "User Name Node", "User Name Node", true, null))
                .build();
    }

    @Override
    public Description getDescription() {
        return DESC;
    }

    @Override
    public ResourceModelSource createResourceModelSource(Properties configuration) throws ConfigurationException {
        MesosAPI mesosAPI = new MesosAPI(createHTTP(configuration, metrics));
        return new MesosResourceModelSource(
                configuration.getProperty(PROPERTY_MESOS_HOST),
                configuration.getProperty(PROPERTY_MESOS_USERNAME),
                mesosAPI.getNodes(""));
    }

    public static DefaultHTTP createHTTP(final Properties properties, final MetricRegistry metrics) {
        String mesosHost = properties.getProperty(PROPERTY_MESOS_HOST);
        String mesosPort = properties.getProperty(PROPERTY_MESOS_PORT);
        return new DefaultHTTP(mesosHost, mesosPort, metrics);
    }
}
