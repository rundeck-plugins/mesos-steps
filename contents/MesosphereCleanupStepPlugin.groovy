import RestClientUtils
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import org.apache.log4j.Logger

/**
 * Created by carlos on 30/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereCleanupStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Cleanup",
        description = "Destroy applications remaining.")
class MesosphereCleanupStepPlugin implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-cleanup-step";
    public static final Logger logger = Logger.getLogger(MesosphereCleanupStepPlugin.class)

    @PluginProperty(title = "Mesos Service Api URL", required = false,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

    @PluginProperty(title = "Api Token", required = false,
            description = "Api Token to Access DC/OS"
    )
    String apiToken

    @PluginProperty(title = "Job UUID", required = true,
            description = "Job UUID to Cleanup"
    )
    String jobUuid

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Delete Cleanup Step Plugin...")

        String mesosNodeName = "";

        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.doCleanup(mesosApiHost, mesosApiToken, mesosNodeName, context)
        logger.info("End execution step - Cleanup Step Plugin...")
    }
}
