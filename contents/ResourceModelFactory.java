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

import java.util.List;
import java.util.Properties;

@Plugin(name = ResourceModelFactory.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.ResourceModelSource)
public class ResourceModelFactory implements ResourceModelSourceFactory, Describable {
    public static final String SERVICE_PROVIDER_NAME = "com.rundeck.plugin.resources.mesos.ResourceModelFactory";

    private static MetricRegistry METRICS = new MetricRegistry();

    static String PROPERTY_MESOS_HOST = "PROPERTY_MESOS_HOST";
    static String PROPERTY_MESOS_PORT = "PROPERTY_MESOS_PORT";
    static String PROPERTY_MESOS_USERNAME = "PROPERTY_MESOS_USERNAME";
    static String PROPERTY_MESOS_SSH_PASSWORD = "PROPERTY_MESOS_SSH_PASSWORD";
    static String PROPERTY_MESOS_SSH_STORAGE_PATH = "PROPERTY_MESOS_SSH_STORAGE_PATH";
    static String PROPERTY_MESOS_EXTRA_TAG = "PROPERTY_MESOS_EXTRA_TAG";
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
                .property(PropertyUtil.string(PROPERTY_MESOS_SSH_PASSWORD, "SSH Password", "SSH Password (Use option.NAME, where NAME is the name of the Job's Secure Remote Authentication Option.)", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_SSH_STORAGE_PATH, "SSH Storage Path", "SSH Storage Path", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_EXTRA_TAG, "Extra tags", "Extra tags", false, null))
                .build();
    }

    @Override
    public Description getDescription() {
        return DESC;
    }

    @Override
    public ResourceModelSource createResourceModelSource(Properties configuration) throws ConfigurationException {
        MesosAPI mesosAPI = new MesosAPI(createHTTP(configuration, metrics));
        List<MesosNode> mesosNodeList = mesosAPI.getNodes("");
        return new MesosResourceModelSource(
                configuration.getProperty(PROPERTY_MESOS_HOST),
                configuration.getProperty(PROPERTY_MESOS_USERNAME),
                configuration.getProperty(PROPERTY_MESOS_SSH_PASSWORD),
                configuration.getProperty(PROPERTY_MESOS_SSH_STORAGE_PATH),
                configuration.getProperty(PROPERTY_MESOS_EXTRA_TAG),
                mesosNodeList);
    }

    public static DefaultHTTP createHTTP(final Properties properties, final MetricRegistry metrics) {
        String mesosHost = properties.getProperty(PROPERTY_MESOS_HOST);
        String mesosPort = properties.getProperty(PROPERTY_MESOS_PORT);
        return new DefaultHTTP(mesosHost, mesosPort, metrics);
    }
}
