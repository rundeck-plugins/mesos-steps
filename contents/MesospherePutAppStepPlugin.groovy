

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions
import com.dtolabs.rundeck.plugins.descriptions.SelectValues
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger

import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.CODE_SYNTAX_MODE
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.DISPLAY_TYPE_KEY
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUPING
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUP_NAME

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
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Mesos Service Config")
            ]
    )
    String mesosServiceApiURL

    @PluginProperty(title = "Api Token", required = false,
            description = "Api Token to Access DC/OS"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Mesos Service Config")
            ]
    )
    String apiToken

    //Main properties

    @PluginProperty(title = "App Id", required = false,
            description = "App Id to PUT on Mesos service."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Main App Config")
            ]
    )
    String id

    @PluginProperty(title = "Cpu's",
            description = "The number of CPU shares this application needs per instance. This number does not have to be integer, but can be a fraction.",
            validatorClass = FractionNumberValidator.class
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = GROUP_NAME, value = "Main App Config")
            ]
    )
    String cpus

    @PluginProperty(title = "Memory",
            description = "The amount of memory in MB that is needed for the application per instance.",
            validatorClass = FractionNumberValidator.class
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = GROUP_NAME, value = "Main App Config")
            ]
    )
    String mem

    @PluginProperty(title = "Disk",
            description = "How much disk space is needed for this application. This number does not have to be an integer, but can be a fraction.",
            validatorClass = FractionNumberValidator.class
    )
    @RenderingOptions(
            [
                    @RenderingOption(key = GROUP_NAME, value = "Main App Config")
            ]
    )
    String disk

    @PluginProperty(title = "Container",
            description = "App Container"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
                @RenderingOption(key = GROUP_NAME, value = "Main App Config")
            ]
    )
    String container

    //END: Main properties

    @PluginProperty(title = "Force",
            defaultValue = 'no',
            description = "Only one deployment can be applied to one application at the same time. If the existing deployment should be canceled by this change, you can set force=yes."
    )
    @SelectValues(values = ['yes', 'no'])
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String force

    @PluginProperty(title = "Partial Update",
            defaultValue = "no",
            description = "Without specifying this parameter, all values that are not defined in the json, will not change existing values."
    )
    @SelectValues(values = ['yes', 'no'])
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String partialUpdate

    @PluginProperty(title = "Backoff Factor",
            description = "Configures exponential backoff behavior when launching potentially sick apps.",
            validatorClass = FractionNumberValidator.class
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String backoffFactor

    @PluginProperty(title = "Backoff Seconds",
            validatorClass = IntegerValidator.class,
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String backoffSeconds

    @PluginProperty(title = "Command",
            description = "The command that is executed.  This value is wrapped by Mesos via /bin/sh -c \${app.cmd}"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String cmd

    @PluginProperty(title = "Dependencies",
            description = "A list of services upon which this application depends."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String dependencies

    @PluginProperty(title = "Executor",
            description = "The executor to use to launch this application. The simplest one (and the one configured by default if none is given) is //cmd"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String executor

    @PluginProperty(title = "Gpus",
            validatorClass = IntegerValidator.class,
            description = "The amount of GPU cores that is needed for the application per instance."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String gpus

    @PluginProperty(title = "Instances",
            validatorClass = IntegerValidator.class,
            description = "The number of instances of this application to start."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String instances

    @PluginProperty(title = "Max Launch Delay Seconds",
            validatorClass = IntegerValidator.class,
            description = "Configures exponential backoff behavior when launching potentially sick apps. This prevents sandboxes associated with consecutively failing tasks"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String maxLaunchDelaySeconds

    @PluginProperty(title = "Health Checks",
            description = "App Health Checks"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String healthChecks

    @PluginProperty(title = "Networks",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String networks

    @PluginProperty(title = "Port Definitions",
            description = "An array of required port resources on the agent host."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String portDefinitions

    @PluginProperty(title = "Ports",
            description = "An array of required port resources on the agent host."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String ports

    @PluginProperty(title = "Readiness Check",
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String readinessCheck

    @PluginProperty(title = "Require Ports",
            defaultValue = "no",
            description = "Applies only for host networking. Normally, the host ports of your tasks are automatically assigned."
    )
    @SelectValues(values = ['yes', 'no'])
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String requirePorts

    @PluginProperty(title = "Residency",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String residency

    @PluginProperty(title = "Secrets",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String secrets

    @PluginProperty(title = "Task Kill Grace Period Seconds",
            validatorClass = IntegerValidator.class,
            description = "Configures the number of seconds between escalating from SIGTERM to SIGKILL when signalling tasks to terminate."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String taskKillGracePeriodSeconds

    @PluginProperty(title = "tty",
            defaultValue = "no",
            description = "Describes if (pseudo) TTY sould be allocated for the process of this container."
    )
    @SelectValues(values = ['yes', 'no'])
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String tty

    @PluginProperty(title = "Unreachable Strategy",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String unreachableStrategy

    @PluginProperty(title = "Upgrade Strategy",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String upgradeStrategy

    @PluginProperty(title = "URIs",
            description = "URIs defined here are resolved, before the application gets started."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String uris

    @PluginProperty(title = "User",
            description = "The user to use to run the tasks on the agent."
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String user

    @PluginProperty(title = "Version",
            description = "The version of this definition"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String version

    @PluginProperty(title = "Version Info",
            description = ""
    )
    @RenderingOptions(
            [
                @RenderingOption(key = GROUP_NAME, value = "Advanced"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String versionInfo

    @PluginProperty(title = "Full JSON",
            description = "Put the whole json of the container to be created - It will override all config settings"
    )
    @RenderingOptions(
            [
                @RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
                @RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
                @RenderingOption(key = GROUP_NAME, value = "Set a JSON config"),
                @RenderingOption(key = GROUPING, value = "secondary")
            ]
    )
    String fullJsonConfig

    @Override
    void executeStep(PluginStepContext context, Map<String, Object> configuration) throws StepException {
        logger.info("Init execution step - Put App Step Plugin...")

        String mesosApiHost = mesosServiceApiURL ?: ProjectPropertiesUtils.getMesosHostPortConfig(context)
        String mesosApiToken = apiToken ?: ProjectPropertiesUtils.getMesosApiTokenConfig(context)

        RestClientUtils.putApp(mesosApiHost, mesosApiToken, id, createMapPropertiesToRequest(),
                [force: (force == "yes"), partialUpdate: (partialUpdate == "yes")], context)
        logger.info("End execution step - Put App Step Plugin...")
    }

    private Map createMapPropertiesToRequest(){
        logger.info("Setting map properties to request...")
        Map propertiesToRequest = [:]
        Map propWithValues = this.properties.findAll {it.value &&
                !['class',
                  'mesosServiceApiURL',
                  'force',
                  'partialUpdate',
                  'mapPropertiesToRequest',
                  'apiToken',
                  'fullJsonConfig'].contains(it.key)}

        if(StringUtils.isNotBlank(fullJsonConfig)){
            def slurper = new JsonSlurper()
            propertiesToRequest = slurper.parseText(fullJsonConfig?.toString())
        } else {
            propWithValues.each { p ->
                def value = p.value
                if(fieldIsJsonType(p.key)){
                    def slurper = new JsonSlurper()
                    propertiesToRequest.put(p.key, slurper.parseText(value?.toString()))
                } else if(fieldIsDoubleType(p.key)) {
                    propertiesToRequest.put(p.key, parseValuesToDouble(value))
                } else if(fieldIsIntegerType(p.key)){
                    propertiesToRequest.put(p.key, parseValuesToInteger(value))
                } else if(fieldHaveArrayValue(p.value)) {
                    propertiesToRequest.put(p.key, value.split(","))
                } else if(fieldIsBooleanType(p.key)) {
                    propertiesToRequest.put(p.key, value == "yes")
                } else {
                    propertiesToRequest.put(p.key, value)
                }
            }
        }


        logger.info("Map properties to request: ${propertiesToRequest}")
        propertiesToRequest
    }

    private boolean fieldIsJsonType(String propName){
        return ["container", "healthChecks", "portDefinitions", "upgradeStrategy"].contains(propName)
    }

    private boolean fieldIsDoubleType(String propName){
        return ["mem", "cpus", "disk", "backoffFactor"].contains(propName)
    }

    private boolean fieldIsBooleanType(String propName){
        return ["tty", "requirePorts", "partialUpdate", "force"].contains(propName)
    }

    private boolean fieldIsIntegerType(String propName){
        return [
                "taskKillGracePeriodSeconds",
                "maxLaunchDelaySeconds",
                "instances",
                "gpus",
                "backoffSeconds"
        ].contains(propName)
    }

    private boolean fieldHaveArrayValue(String value){
        return value instanceof String && value.contains(',')
    }

    private Double parseValuesToDouble(String value){
        return value ? Double.parseDouble(value) : null
    }

    private Double parseValuesToInteger(String value){
        return value ? Integer.parseInteger(value) : null
    }
}
