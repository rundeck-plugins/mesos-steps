package com.rundeck.plugin

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.step.StepPlugin
import com.rundeck.plugin.util.RestClientUtils

/**
 * Created by carlos on 30/12/17.
 */
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereDeleteTasksStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Delete Tasks",
        description = "Kill tasks that belong to the application app_id")
class MesosphereDeleteTasksStepPlugin implements StepPlugin{
    public static final String PROVIDER_NAME = "mesos-delete-tasks-step"

    @PluginProperty(title = "Mesos Service Api URL", required = true,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

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
        RestClientUtils.deleteTaskApp(mesosServiceApiURL, id,
                [force: force, host: host, scale: scale, wipe: wipe], context)
    }
}
