

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.CODE_SYNTAX_MODE
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.DISPLAY_TYPE_KEY

/**
 * Created by carlos on 29/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesospherePutAppStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Put App",
        description = "Change multiple applications either by upgrading existing ones or creating new ones.")
public class MesospherePutAppStepPlugin implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-put-app-step";
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
            description = "App Id to PUT on Mesos service."
    )
    String id

    @PluginProperty(title = "Force",
            description = "Only one deployment can be applied to one application at the same time. If the existing deployment should be canceled by this change, you can set force=true."
    )
    boolean force

    @PluginProperty(title = "Partial Update",
            defaultValue = "true",
            description = "Without specifying this parameter, all values that are not defined in the json, will not change existing values."
    )
    boolean partialUpdate

    @PluginProperty(title = "Backoff Factor",
            description = "Configures exponential backoff behavior when launching potentially sick apps.",
            validatorClass = FractionNumberValidator.class
    )
    String backoffFactor

    @PluginProperty(title = "Backoff Seconds",
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    Integer backoffSeconds

    @PluginProperty(title = "Command",
            description = "The command that is executed.  This value is wrapped by Mesos via /bin/sh -c \${app.cmd}"
    )
    String cmd

    @PluginProperty(title = "Cpu's",
            description = "The number of CPU shares this application needs per instance. This number does not have to be integer, but can be a fraction.",
            validatorClass = FractionNumberValidator.class
    )
    String cpus

    @PluginProperty(title = "Dependencies",
            description = "A list of services upon which this application depends."
    )
    String dependencies

    @PluginProperty(title = "Disk",
            description = "How much disk space is needed for this application. This number does not have to be an integer, but can be a fraction.",
            validatorClass = FractionNumberValidator.class
    )
    String disk

    @PluginProperty(title = "Executor",
            description = "The executor to use to launch this application. The simplest one (and the one configured by default if none is given) is //cmd"
    )
    String executor

    @PluginProperty(title = "Gpus",
            description = "The amount of GPU cores that is needed for the application per instance."
    )
    Integer gpus

    @PluginProperty(title = "Instances",
            description = "The number of instances of this application to start."
    )
    Integer instances

    @PluginProperty(title = "Max Launch Delay Seconds",
            description = "Configures exponential backoff behavior when launching potentially sick apps. This prevents sandboxes associated with consecutively failing tasks"
    )
    Integer maxLaunchDelaySeconds

    @PluginProperty(title = "Container",
            description = "App Container"
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                    @RenderingOption(key = CODE_SYNTAX_MODE, value = "json")
            ]
    )
    String container

    @PluginProperty(title = "Health Checks",
            description = "App Health Checks"
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                    @RenderingOption(key = CODE_SYNTAX_MODE, value = "json")
            ]
    )
    String healthChecks

    @PluginProperty(title = "Memory",
            description = "The amount of memory in MB that is needed for the application per instance.",
            validatorClass = FractionNumberValidator.class
    )
    String mem

    @PluginProperty(title = "Networks",
            description = ""
    )
    String networks

    @PluginProperty(title = "Port Definitions",
            description = "An array of required port resources on the agent host."
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                    @RenderingOption(key = CODE_SYNTAX_MODE, value = "json")
            ]
    )
    String portDefinitions

    @PluginProperty(title = "Ports",
            description = "An array of required port resources on the agent host."
    )
    String ports

    @PluginProperty(title = "Readiness Check",
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    String readinessCheck

    @PluginProperty(title = "Require Ports",
            description = "Applies only for host networking. Normally, the host ports of your tasks are automatically assigned."
    )
    boolean requirePorts

    @PluginProperty(title = "Residency",
            description = ""
    )
    String residency

    @PluginProperty(title = "Secrets",
            description = ""
    )
    String secrets

    @PluginProperty(title = "Task Kill Grace Period Seconds",
            description = "Configures the number of seconds between escalating from SIGTERM to SIGKILL when signalling tasks to terminate."
    )
    Integer taskKillGracePeriodSeconds

    @PluginProperty(title = "tty",
            description = "Describes if (pseudo) TTY sould be allocated for the process of this container."
    )
    boolean tty

    @PluginProperty(title = "Unreachable Strategy",
            description = ""
    )
    String unreachableStrategy

    @PluginProperty(title = "Upgrade Strategy",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json")
            ]
    )
    String upgradeStrategy

    @PluginProperty(title = "URIs",
            description = "URIs defined here are resolved, before the application gets started."
    )
    String uris

    @PluginProperty(title = "User",
            description = "The user to use to run the tasks on the agent."
    )
    String user

    @PluginProperty(title = "Version",
            description = "The version of this definition"
    )
    Date version

    @PluginProperty(title = "Version Info",
            description = ""
    )
    String versionInfo

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Put App Step Plugin...")

        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.putApp(mesosApiHost, mesosApiToken, id, createMapPropertiesToRequest(),
                [force: force, partialUpdate: partialUpdate], context)
        logger.info("End execution step - Put App Step Plugin...")
    }

    private Map createMapPropertiesToRequest(){
        logger.info("Setting map properties to request...")
        Map propertiesToRequest = [:]
        Map propWithValues = this.properties.findAll {it.value &&
                !['class', 'mesosServiceApiURL', 'force', 'partialUpdate', 'mapPropertiesToRequest', 'apiToken'].contains(it.key)}

        propWithValues.each { p ->
            def value = p.value
            if(["container", "healthChecks", "portDefinitions", "upgradeStrategy"].contains(p.key)){
                def slurper = new JsonSlurper()
                propertiesToRequest.put(p.key, slurper.parseText(value?.toString()))
            } else if(p.value instanceof String && p.value.contains(',')) {
                propertiesToRequest.put(p.key, value.split(","))
            } else if(["mem", "cpus", "disk", "backoffFactor"].contains(p.key)){
                propertiesToRequest.put(p.key, parseValuesToDouble(value))
            } else {
                propertiesToRequest.put(p.key, value)
            }
        }

        logger.info("Map properties to request: ${propertiesToRequest}")
        propertiesToRequest
    }

    private Double parseValuesToDouble(String value){
        return value ? Double.parseDouble(value) : null
    }
}
