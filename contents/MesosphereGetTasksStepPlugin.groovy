

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin

/**
 * Created by carlos on 30/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereGetTasksStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Get Tasks",
        description = "The list of running tasks for application app_id.")
class MesosphereGetTasksStepPlugin  implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-get-tasks-step";

    @PluginProperty(title = "Mesos Service Api URL", required = true,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

    @PluginProperty(title = "App Id", required = true,
            description = "App Id on Mesos service."
    )
    String id

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        RestClientUtils.getTaskApp(mesosServiceApiURL, id, context)
    }
}
