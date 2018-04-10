import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

import static java.lang.String.format;

public class ProjectPropertiesUtils {
    public static final String PROJECT_PROPERTY_API_TOKEN = "project.mesos.apitoken";
    public static final String PROJECT_PROPERTY_HOST = "project.mesos.host";
    public static final String PROJECT_PROPERTY_API_CONTEXT = "project.mesos.marathon.context";
    public static final String PROJECT_PROPERTY_PORT = "project.mesos.port";

    static String getMesosApiTokenConfig(PluginStepContext context){
        return getValueOf(PROJECT_PROPERTY_API_TOKEN, context);
    }

    static String getMesosHostPortConfig(PluginStepContext context){
        return format("%s:%s/%s",
                getValueOf(PROJECT_PROPERTY_HOST, context),
                getValueOf(PROJECT_PROPERTY_PORT, context),
                getValueOf(PROJECT_PROPERTY_API_CONTEXT, context));
    }

    static String getMesosApiTokenConfig(Framework framework, String projectName){
        return getValueOf(PROJECT_PROPERTY_API_TOKEN, framework, projectName);
    }

    static String getMesosHostConfig(Framework framework, String projectName){
        return getValueOf(PROJECT_PROPERTY_HOST, framework, projectName);
    }

    static String getMesosApiContextConfig(Framework framework, String projectName){
        return getValueOf(PROJECT_PROPERTY_API_CONTEXT, framework, projectName);
    }

    static String getMesosPortConfig(Framework framework, String projectName){
        return getValueOf(PROJECT_PROPERTY_PORT, framework, projectName);
    }

    static String getValueOf(String name, PluginStepContext context){
        return context.getFramework()
                .getFrameworkProjectMgr().getFrameworkProject(context.getFrameworkProject()).getProperty(name);
    }

    static String getValueOf(String name, Framework framework, String projectName){
        return framework.getFrameworkProjectMgr().getFrameworkProject(projectName).getProperty(name);
    }
}
