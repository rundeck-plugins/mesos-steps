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
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

@Plugin(name = ResourceModelFactory.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.ResourceModelSource)
public class ResourceModelFactory implements ResourceModelSourceFactory, Describable {
    public static final String SERVICE_PROVIDER_NAME = "com.rundeck.plugin.resources.mesos.ResourceModelFactory";
    public static final Logger logger = Logger.getLogger(ResourceModelFactory.class);

    private static MetricRegistry METRICS = new MetricRegistry();

    static String PROPERTY_MESOS_HOST = "host";
    static String PROPERTY_MESOS_PORT = "port";
    static String PROPERTY_MESOS_API_CONTEXT = "api_context";
    static String PROPERTY_MESOS_USERNAME = "username";
    static String PROPERTY_MESOS_SSH_PASSWORD = "ssh_password";
    static String PROPERTY_MESOS_SSH_STORAGE_PATH = "ssh_storage_path";
    static String PROPERTY_MESOS_EXTRA_TAG = "extra_tag";
    static String PROPERTY_MESOS_API_TOKEN = "api_token";
    static String PROPERTY_FILTER_NODE_PREFIX = "filter_node_prefix";

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
                .property(PropertyUtil.string(PROPERTY_MESOS_API_CONTEXT, "Mesos Api Marathon Context", "Mesos Api Marathon Context (i.g., 'service/marathon')", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_USERNAME, "User Name Node", "User Name Node", true, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_SSH_PASSWORD, "SSH Password", "SSH Password (Use option.NAME, where NAME is the name of the Job's Secure Remote Authentication Option.)", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_SSH_STORAGE_PATH, "SSH Storage Path", "SSH Storage Path", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_EXTRA_TAG, "Extra tags", "Extra tags", false, null))
                .property(PropertyUtil.string(PROPERTY_MESOS_API_TOKEN, "API TOKEN", "Mesos API Token to get nodes info", false, null))
                .property(PropertyUtil.string(PROPERTY_FILTER_NODE_PREFIX, "Filter Node Prefix", "only retrieve the nodes for contexts that start with that and not show the rest of the nodes in mesos", false, null))
                .build();
    }

    @Override
    public Description getDescription() {
        return DESC;
    }

    @Override
    public ResourceModelSource createResourceModelSource(Properties configuration) throws ConfigurationException {
        logger.info("Creating resources model sources...");
        MesosAPI mesosAPI = new MesosAPI(createHTTP(configuration, metrics, this.framework, configuration.getProperty("project")));

        String mesosApiContext = configuration.getProperty(PROPERTY_MESOS_API_CONTEXT) != null ?
                configuration.getProperty(PROPERTY_MESOS_API_CONTEXT) :
                ProjectPropertiesUtils.getMesosApiContextConfig(framework, configuration.getProperty("project"));

        List<MesosNode> mesosNodeList = mesosAPI.getNodes(mesosApiContext);
        return new MesosResourceModelSource(
                configuration.getProperty(PROPERTY_MESOS_HOST),
                configuration.getProperty(PROPERTY_MESOS_USERNAME),
                configuration.getProperty(PROPERTY_MESOS_SSH_PASSWORD),
                configuration.getProperty(PROPERTY_MESOS_SSH_STORAGE_PATH),
                configuration.getProperty(PROPERTY_MESOS_EXTRA_TAG),
                configuration.getProperty(PROPERTY_FILTER_NODE_PREFIX),
                mesosNodeList);
    }

    public static DefaultHTTP createHTTP(final Properties properties, final MetricRegistry metrics, final Framework framework, final String projectName) {
        logger.info("Creating HTTP request to Mesos Service...");
        String mesosApiToken = properties.getProperty(PROPERTY_MESOS_API_TOKEN) != null ?
                properties.getProperty(PROPERTY_MESOS_API_TOKEN) :
                ProjectPropertiesUtils.getMesosApiTokenConfig(framework, projectName);

        String mesosHost = properties.getProperty(PROPERTY_MESOS_HOST) != null ?
                properties.getProperty(PROPERTY_MESOS_HOST) :
                ProjectPropertiesUtils.getMesosHostConfig(framework, projectName);

        String mesosPort = properties.getProperty(PROPERTY_MESOS_PORT) != null ?
                properties.getProperty(PROPERTY_MESOS_PORT) :
                ProjectPropertiesUtils.getMesosPortConfig(framework, projectName);

        logger.info(format("Mesos Host: %s", mesosHost));
        logger.info(format("Mesos Port: %s", mesosPort));

        return new DefaultHTTP(mesosHost, mesosPort, mesosApiToken, metrics);
    }
}
