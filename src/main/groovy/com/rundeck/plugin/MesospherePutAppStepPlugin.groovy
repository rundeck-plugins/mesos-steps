package com.rundeck.plugin

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Created by carlos on 29/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesospherePutAppStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Put App",
        description = "Change multiple applications either by upgrading existing ones or creating new ones.")
public class MesospherePutAppStepPlugin implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-put-app-step";
    public static final String URI_PATH = "/v2/apps/"

    @PluginProperty(title = "Mesos Service Api URL", required = true,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

    @PluginProperty(title = "App Id", required = true,
            description = "App Id to PUT on Mesos service."
    )
    String id

    @PluginProperty(title = "Backoff Factor",
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    long backoffFactor

    @PluginProperty(title = "Backoff Seconds",
            description = "Configures exponential backoff behavior when launching potentially sick apps."
    )
    Integer backoffSeconds

    @PluginProperty(title = "Command",
            required = true,
            description = "The command that is executed.  This value is wrapped by Mesos via /bin/sh -c \${app.cmd}"
    )
    String cmd

    @PluginProperty(title = "Cpu's",
            description = "The number of CPU shares this application needs per instance. This number does not have to be integer, but can be a fraction."
    )
    long cpus

    @PluginProperty(title = "Dependencies",
            description = "The number of CPU shares this application needs per instance. This number does not have to be integer, but can be a fraction."
    )
    String dependencies

    @PluginProperty(title = "Disk",
            description = "How much disk space is needed for this application. This number does not have to be an integer, but can be a fraction."
    )
    long disk

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

    @PluginProperty(title = "Memory",
            description = "The amount of memory in MB that is needed for the application per instance."
    )
    long mem

    @PluginProperty(title = "Networks",
            description = ""
    )
    String networks

    @PluginProperty(title = "Port Definitions",
            description = "An array of required port resources on the agent host."
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
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)

        serviceAPI.request(Method.POST, ContentType.JSON){ req ->
            uri.path = URI_PATH
            uri.query = [format:'json']
            body = getMapPropertiesToRequest()

            response.success = { resp, json ->
                assert [200, 201].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        e,
                        MesosFailReason.PutAppError
                );
            }
        }

    }

    public enum MesosFailReason implements FailureReason {
        PutAppError
    }

    private Map getMapPropertiesToRequest(){
        Map propertiesToRequest = [:]
        Map propWithValues = this.properties.findAll {it.value && !['class', 'mesosServiceApiURL'].contains(it.key)}

        propWithValues.each { p ->
            def value = p.value
            if(p.value instanceof String && p.value.contains(',')){
                propertiesToRequest.put(p.key, value.split(","))
            } else {
                propertiesToRequest.put(p.key, value)
            }
        }

        propertiesToRequest
    }
}
