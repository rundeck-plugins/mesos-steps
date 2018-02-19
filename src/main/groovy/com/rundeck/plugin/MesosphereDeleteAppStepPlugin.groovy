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
@Plugin(service = ServiceNameConstants.WorkflowStep, name = MesosphereDeleteAppStepPlugin.PROVIDER_NAME)
@PluginDescription(title = "Mesos Delete App",
        description = "Destroy an application. All data about that application will be deleted.")
class MesosphereDeleteAppStepPlugin implements StepPlugin {
    public static final String PROVIDER_NAME = "mesos-delete-app-step";

    @PluginProperty(title = "Mesos Service Api URL", required = true,
            description = "Address to access mesos service api."
    )
    String mesosServiceApiURL

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
        RestClientUtils.deleteApp(mesosServiceApiURL, id, [force: force], context)
    }
}
