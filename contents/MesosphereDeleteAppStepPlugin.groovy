import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import RestClientUtils
import org.apache.log4j.Logger

/**
 * Created by carlos on 30/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereDeleteAppStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Delete App",
        description = "Destroy an application. All data about that application will be deleted.")
class MesosphereDeleteAppStepPlugin implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-delete-app-step";
    public static final Logger logger = Logger.getLogger(MesosphereDeleteAppStepPlugin.class)

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
            description = "Only one deployment can be applied to one application at the same time. If the existing deployment should be canceled by this change, you can set force=true."
    )
    boolean force

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Delete App Step Plugin...")
        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.deleteApp(mesosApiHost, mesosApiToken, id, [force: force], context)
        logger.info("End execution step - Delete App Step Plugin...")
    }
}
