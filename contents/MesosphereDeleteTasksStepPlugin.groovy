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
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereDeleteTasksStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Delete Tasks",
        description = "Kill tasks that belong to the application app_id")
class MesosphereDeleteTasksStepPlugin implements StepPlugin{
    public static final String PROVIDER_NAME = "mesos-delete-tasks-step"
    public static final Logger logger = Logger.getLogger(MesospherePutAppStepPlugin.class)

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

    @PluginProperty(title = "Force",
            description = "If the existing deployment should be canceled by this change, you can set force=true."
    )
    boolean force

    @PluginProperty(title = "Host", required = true,
            description = "all tasks of that application on the supplied slave are killed"
    )
    String host

    @PluginProperty(title = "Scale",
            description = "If scale=true is specified, then the application is scaled down by the number of killed tasks."
    )
    boolean scale

    @PluginProperty(title = "Wipe",
            description = "If wipe=true is specified and the app uses local persistent volumes, associated dynamic reservations will be unreserved, and persistent volumes will be destroyed. Only possible if scale=false or not specified."
    )
    boolean wipe

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Delete Tast from App Step Plugin...")
        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.deleteTaskApp(mesosApiHost, mesosApiToken, id?.toLowerCase(),
                [force: force, host: host, scale: scale, wipe: wipe], context)
        logger.info("End execution step - Delete Tast from App Step Plugin...")
    }
}
