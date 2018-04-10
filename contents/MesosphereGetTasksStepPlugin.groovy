

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
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereGetTasksStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Get Tasks",
        description = "The list of running tasks for application app_id.")
class MesosphereGetTasksStepPlugin  implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-get-tasks-step";
    public static final Logger logger = Logger.getLogger(MesosphereGetTasksStepPlugin.class)

    @PluginProperty(title = "Mesos Service Api URL", required = false,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

    @PluginProperty(title = "Api Token", required = false,
            description = "Api Token to Access DC/OS"
    )
    String apiToken

    @PluginProperty(title = "App Id", required = true,
            description = "App Id on Mesos service."
    )
    String id

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Get Tasks Step Plugin...")
        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.getTaskApp(mesosApiHost, mesosApiToken, id, context)
        logger.info("End execution step - Get Tasks Step Plugin...")
    }
}
